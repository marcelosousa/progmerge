package org.jetbrains.jet.lang.resolve;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.JetSemanticServices;
import org.jetbrains.jet.lang.cfg.JetControlFlowProcessor;
import org.jetbrains.jet.lang.cfg.JetFlowInformationProvider;
import org.jetbrains.jet.lang.cfg.LoopInfo;
import org.jetbrains.jet.lang.cfg.pseudocode.*;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.descriptors.annotations.AnnotationDescriptor;
import org.jetbrains.jet.lang.diagnostics.Errors;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.resolve.scopes.JetScope;
import org.jetbrains.jet.lang.resolve.scopes.WritableScope;
import org.jetbrains.jet.lang.resolve.scopes.WritableScopeImpl;
import org.jetbrains.jet.lang.types.*;
import org.jetbrains.jet.lexer.JetTokens;

import java.util.*;

import static org.jetbrains.jet.lang.diagnostics.Errors.*;

/**
 * @author abreslav
 */
public class ClassDescriptorResolver {
    private final JetSemanticServices semanticServices;
    private final TypeResolver typeResolver;
    private final TypeResolver typeResolverNotCheckingBounds;
    private final BindingTrace trace;
    private final JetControlFlowDataTraceFactory flowDataTraceFactory;
    private final AnnotationResolver annotationResolver;

    public ClassDescriptorResolver(JetSemanticServices semanticServices, BindingTrace trace, JetControlFlowDataTraceFactory flowDataTraceFactory) {
        this.semanticServices = semanticServices;
        this.typeResolver = new TypeResolver(semanticServices, trace, true);
        this.typeResolverNotCheckingBounds = new TypeResolver(semanticServices, trace, false);
        this.trace = trace;
        this.flowDataTraceFactory = flowDataTraceFactory;
        this.annotationResolver = new AnnotationResolver(semanticServices, trace);
    }

    public void resolveMutableClassDescriptor(@NotNull JetClass classElement, @NotNull MutableClassDescriptor descriptor) {
        // TODO : Where-clause
        List<TypeParameterDescriptor> typeParameters = Lists.newArrayList();
        int index = 0;
        for (JetTypeParameter typeParameter : classElement.getTypeParameters()) {
            TypeParameterDescriptor typeParameterDescriptor = TypeParameterDescriptor.createForFurtherModification(
                    descriptor,
                    annotationResolver.createAnnotationStubs(typeParameter.getModifierList()),
                    typeParameter.getVariance(),
                    JetPsiUtil.safeName(typeParameter.getName()),
                    index
            );
            trace.record(BindingContext.TYPE_PARAMETER, typeParameter, typeParameterDescriptor);
            typeParameters.add(typeParameterDescriptor);
            index++;
        }
        descriptor.setTypeParameterDescriptors(typeParameters);
        Modality defaultModality = descriptor.getKind() == ClassKind.TRAIT ? Modality.ABSTRACT : Modality.FINAL;
        descriptor.setModality(resolveModalityFromModifiers(classElement.getModifierList(), defaultModality));
        descriptor.setVisibility(resolveVisibilityFromModifiers(classElement.getModifierList()));

        trace.record(BindingContext.CLASS, classElement, descriptor);
    }

    public void resolveSupertypes(@NotNull JetClassOrObject jetClass, @NotNull MutableClassDescriptor descriptor) {
        List<JetDelegationSpecifier> delegationSpecifiers = jetClass.getDelegationSpecifiers();
        if (delegationSpecifiers.isEmpty()) {
            descriptor.addSupertype(getDefaultSupertype(jetClass));
        }
        else {
            Collection<JetType> supertypes = resolveDelegationSpecifiers(
                    descriptor.getScopeForSupertypeResolution(),
                    delegationSpecifiers,
                    typeResolverNotCheckingBounds);
            for (JetType supertype : supertypes) {
                descriptor.addSupertype(supertype);
            }
        }

    }

    private JetType getDefaultSupertype(JetClassOrObject jetClass) {
        // TODO : beautify
        if (jetClass instanceof JetEnumEntry) {
            JetClassOrObject parent = PsiTreeUtil.getParentOfType(jetClass, JetClassOrObject.class);
            ClassDescriptor parentDescriptor = trace.getBindingContext().get(BindingContext.CLASS, parent);
            if (parentDescriptor.getTypeConstructor().getParameters().isEmpty()) {
                return parentDescriptor.getDefaultType();
            }
            else {
//                trace.getErrorHandler().genericError(((JetEnumEntry) jetClass).getNameIdentifier().getNode(), "Generic arguments of the base type must be specified");
                trace.report(NO_GENERICS_IN_SUPERTYPE_SPECIFIER.on(((JetEnumEntry) jetClass).getNameIdentifier()));
                return ErrorUtils.createErrorType("Supertype not specified");
            }
        }
        return JetStandardClasses.getAnyType();
    }

    public Collection<JetType> resolveDelegationSpecifiers(JetScope extensibleScope, List<JetDelegationSpecifier> delegationSpecifiers, @NotNull TypeResolver resolver) {
        if (delegationSpecifiers.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<JetType> result = Lists.newArrayList();
        for (JetDelegationSpecifier delegationSpecifier : delegationSpecifiers) {
            JetTypeReference typeReference = delegationSpecifier.getTypeReference();
            if (typeReference != null) {
                result.add(resolver.resolveType(extensibleScope, typeReference));
                JetTypeElement typeElement = typeReference.getTypeElement();
                while (typeElement instanceof JetNullableType) {
                    JetNullableType nullableType = (JetNullableType) typeElement;
//                    trace.getErrorHandler().genericError(nullableType.getQuestionMarkNode(), "A supertype cannot be nullable");
                    trace.report(NULLABLE_SUPERTYPE.on(nullableType.getQuestionMarkNode()));
                    typeElement = nullableType.getInnerType();
                }
                if (typeElement instanceof JetUserType) {
                    JetUserType userType = (JetUserType) typeElement;
                    List<JetTypeProjection> typeArguments = userType.getTypeArguments();
                    for (JetTypeProjection typeArgument : typeArguments) {
                        if (typeArgument.getProjectionKind() != JetProjectionKind.NONE) {
//                            trace.getErrorHandler().genericError(typeArgument.getProjectionNode(), "Projections are not allowed for immediate arguments of a supertype");
                            trace.report(PROJECTION_IN_IMMEDIATE_ARGUMENT_TO_SUPERTYPE.on(typeArgument.getProjectionNode()));
                        }
                    }
                }
            }
            else {
                result.add(ErrorUtils.createErrorType("No type reference"));
            }
        }
        return result;
    }

    @NotNull
    public FunctionDescriptorImpl resolveFunctionDescriptor(DeclarationDescriptor containingDescriptor, final JetScope scope, final JetNamedFunction function) {
        final FunctionDescriptorImpl functionDescriptor = new FunctionDescriptorImpl(
                containingDescriptor,
                annotationResolver.resolveAnnotations(scope, function.getModifierList()),
                JetPsiUtil.safeName(function.getName())
        );
        WritableScope innerScope = new WritableScopeImpl(scope, functionDescriptor, new TraceBasedRedeclarationHandler(trace)).setDebugName("Function descriptor header scope");
        innerScope.addLabeledDeclaration(functionDescriptor);

        List<TypeParameterDescriptor> typeParameterDescriptors = resolveTypeParameters(functionDescriptor, innerScope, function.getTypeParameters());
        resolveGenericBounds(function, innerScope, typeParameterDescriptors);

        JetType receiverType = null;
        JetTypeReference receiverTypeRef = function.getReceiverTypeRef();
        if (receiverTypeRef != null) {
            JetScope scopeForReceiver =
                    function.hasTypeParameterListBeforeFunctionName()
                            ? innerScope
                            : scope;
            receiverType = typeResolver.resolveType(scopeForReceiver, receiverTypeRef);
        }

        List<ValueParameterDescriptor> valueParameterDescriptors = resolveValueParameters(functionDescriptor, innerScope, function.getValueParameters());

        JetTypeReference returnTypeRef = function.getReturnTypeRef();
        JetType returnType;
        if (returnTypeRef != null) {
            returnType = typeResolver.resolveType(innerScope, returnTypeRef);
        }
        else if (function.hasBlockBody()) {
            returnType = JetStandardClasses.getUnitType();
        }
        else {
            final JetExpression bodyExpression = function.getBodyExpression();
            if (bodyExpression != null) {
                returnType = new DeferredType(new LazyValue<JetType>() {
                    @Override
                    protected JetType compute() {
                        JetFlowInformationProvider flowInformationProvider = computeFlowData(function, bodyExpression);
                        return semanticServices.getTypeInferrerServices(trace, flowInformationProvider).inferFunctionReturnType(scope, function, functionDescriptor);
                    }
                });
            }
            else {
//                trace.getErrorHandler().genericError(function.asElement().getNode(), "This function must either declare a return type or have a body element");
                trace.report(FUNCTION_WITH_NO_TYPE_NO_BODY.on(function.asElement()));
                returnType = ErrorUtils.createErrorType("No type, no body");
            }
        }
        boolean hasBody = function.getBodyExpression() != null;
        Modality defaultModality = getDefaultModality(containingDescriptor, hasBody);
        Modality modality = resolveModalityFromModifiers(function.getModifierList(), defaultModality);
        Visibility visibility = resolveVisibilityFromModifiers(function.getModifierList());
        functionDescriptor.initialize(
                receiverType,
                typeParameterDescriptors,
                valueParameterDescriptors,
                returnType,
                modality,
                visibility);

        trace.record(BindingContext.FUNCTION, function, functionDescriptor);
        return functionDescriptor;
    }

    private Modality getDefaultModality(DeclarationDescriptor containingDescriptor, boolean isBodyPresent) {
        Modality defaultModality;
        if (containingDescriptor instanceof ClassDescriptor) {
            boolean isTrait = ((ClassDescriptor) containingDescriptor).getKind() == ClassKind.TRAIT;
            boolean isDefinitelyAbstract = isTrait && !isBodyPresent;
            Modality basicModality = isTrait ? Modality.OPEN : Modality.FINAL;
            defaultModality = isDefinitelyAbstract ? Modality.ABSTRACT : basicModality;
        }
        else {
            defaultModality = Modality.FINAL;
        }
        return defaultModality;
    }

    @NotNull
    private List<ValueParameterDescriptor> resolveValueParameters(FunctionDescriptor functionDescriptor, WritableScope parameterScope, List<JetParameter> valueParameters) {
        List<ValueParameterDescriptor> result = new ArrayList<ValueParameterDescriptor>();
        for (int i = 0, valueParametersSize = valueParameters.size(); i < valueParametersSize; i++) {
            JetParameter valueParameter = valueParameters.get(i);
            JetTypeReference typeReference = valueParameter.getTypeReference();

            ASTNode valOrVarNode = valueParameter.getValOrVarNode();
            if (valueParameter.isRef() && valOrVarNode != null) {
//                trace.getErrorHandler().genericError(valOrVarNode, "'val' and 'var' are not allowed on ref-parameters");
                trace.report(REF_PARAMETER_WITH_VAL_OR_VAR.on(valOrVarNode));
            }

            JetType type;
            if (typeReference == null) {
//                trace.getErrorHandler().genericError(valueParameter.getNode(), "A type annotation is required on a value parameter");
                trace.report(VALUE_PARAMETER_WITH_NO_TYPE_ANNOTATION.on(valueParameter));
                type = ErrorUtils.createErrorType("Type annotation was missing");
            } else {
                type = typeResolver.resolveType(parameterScope, typeReference);
            }

            ValueParameterDescriptor valueParameterDescriptor = resolveValueParameterDescriptor(functionDescriptor, valueParameter, i, type);
            parameterScope.addVariableDescriptor(valueParameterDescriptor);
            result.add(valueParameterDescriptor);
        }
        return result;
    }

    @NotNull
    public MutableValueParameterDescriptor resolveValueParameterDescriptor(DeclarationDescriptor declarationDescriptor, JetParameter valueParameter, int index, JetType type) {
        MutableValueParameterDescriptor valueParameterDescriptor = new ValueParameterDescriptorImpl(
            declarationDescriptor,
            index,
            annotationResolver.createAnnotationStubs(valueParameter.getModifierList()),
            JetPsiUtil.safeName(valueParameter.getName()),
            valueParameter.isMutable() ? type : null,
            type,
            valueParameter.getDefaultValue() != null,
            false // TODO : varargs
    );
        // TODO : Default values???

        trace.record(BindingContext.VALUE_PARAMETER, valueParameter, valueParameterDescriptor);
        return valueParameterDescriptor;
    }

    public List<TypeParameterDescriptor> resolveTypeParameters(DeclarationDescriptor containingDescriptor, WritableScope extensibleScope, List<JetTypeParameter> typeParameters) {
        List<TypeParameterDescriptor> result = new ArrayList<TypeParameterDescriptor>();
        for (int i = 0, typeParametersSize = typeParameters.size(); i < typeParametersSize; i++) {
            JetTypeParameter typeParameter = typeParameters.get(i);
            result.add(resolveTypeParameter(containingDescriptor, extensibleScope, typeParameter, i));
        }
        return result;
    }

    private TypeParameterDescriptor resolveTypeParameter(DeclarationDescriptor containingDescriptor, WritableScope extensibleScope, JetTypeParameter typeParameter, int index) {
//        JetTypeReference extendsBound = typeParameter.getExtendsBound();
//        JetType bound = extendsBound == null
//                ? JetStandardClasses.getDefaultBound()
//                : typeResolver.resolveType(extensibleScope, extendsBound);
        TypeParameterDescriptor typeParameterDescriptor = TypeParameterDescriptor.createForFurtherModification(
                containingDescriptor,
                annotationResolver.createAnnotationStubs(typeParameter.getModifierList()),
                typeParameter.getVariance(),
                JetPsiUtil.safeName(typeParameter.getName()),
                index
        );
//        typeParameterDescriptor.addUpperBound(bound);
        extensibleScope.addTypeParameterDescriptor(typeParameterDescriptor);
        trace.record(BindingContext.TYPE_PARAMETER, typeParameter, typeParameterDescriptor);
        return typeParameterDescriptor;
    }

    public void resolveGenericBounds(@NotNull JetTypeParameterListOwner declaration, JetScope scope, List<TypeParameterDescriptor> parameters) {
        List<JetTypeParameter> typeParameters = declaration.getTypeParameters();
        Map<String, TypeParameterDescriptor> parameterByName = Maps.newHashMap();
        for (int i = 0, typeParametersSize = typeParameters.size(); i < typeParametersSize; i++) {
            JetTypeParameter jetTypeParameter = typeParameters.get(i);
            TypeParameterDescriptor typeParameterDescriptor = parameters.get(i);
            parameterByName.put(typeParameterDescriptor.getName(), typeParameterDescriptor);
            JetTypeReference extendsBound = jetTypeParameter.getExtendsBound();
            if (extendsBound != null) {
                typeParameterDescriptor.addUpperBound(resolveAndCheckUpperBoundType(extendsBound, scope, false));
            }
        }
        for (JetTypeConstraint constraint : declaration.getTypeConstaints()) {
            JetSimpleNameExpression subjectTypeParameterName = constraint.getSubjectTypeParameterName();
            if (subjectTypeParameterName == null) {
                continue;
            }
            String referencedName = subjectTypeParameterName.getReferencedName();
            if (referencedName == null) {
                continue;
            }
            TypeParameterDescriptor typeParameterDescriptor = parameterByName.get(referencedName);
            JetTypeReference boundTypeReference = constraint.getBoundTypeReference();
            JetType bound = boundTypeReference != null ? resolveAndCheckUpperBoundType(boundTypeReference, scope, constraint.isClassObjectContraint()) : null;
            if (typeParameterDescriptor == null) {
                // To tell the user that we look only for locally defined type parameters
                ClassifierDescriptor classifier = scope.getClassifier(referencedName);
                if (classifier != null) {
//                    trace.getErrorHandler().genericError(subjectTypeParameterName.getNode(), referencedName + " does not refer to a type parameter of " + declaration.getName());
                    trace.report(NAME_IN_CONSTRAINT_IS_NOT_A_TYPE_PARAMETER.on(subjectTypeParameterName, constraint, declaration));
                    trace.record(BindingContext.REFERENCE_TARGET, subjectTypeParameterName, classifier);
                }
                else {
                    trace.report(UNRESOLVED_REFERENCE.on(subjectTypeParameterName));
                }
            }
            else {
                trace.record(BindingContext.REFERENCE_TARGET, subjectTypeParameterName, typeParameterDescriptor);
                if (bound != null) {
                    if (constraint.isClassObjectContraint()) {
                        typeParameterDescriptor.addClassObjectBound(bound);
                    }
                    else {
                        typeParameterDescriptor.addUpperBound(bound);
                    }
                }
            }
        }

        for (TypeParameterDescriptor parameter : parameters) {
            if (parameter.getUpperBounds().isEmpty()) {
                parameter.addUpperBound(JetStandardClasses.getDefaultBound());
            }

            if (JetStandardClasses.isNothing(parameter.getBoundsAsType())) {
                PsiElement nameIdentifier = typeParameters.get(parameter.getIndex()).getNameIdentifier();
                if (nameIdentifier != null) {
//                    trace.getErrorHandler().genericError(nameIdentifier.getNode(), "Upper bounds of " + parameter.getName() + " have empty intersection");
                    trace.report(CONFLICTING_UPPER_BOUNDS.on(nameIdentifier, parameter));
                }
            }

            JetType classObjectType = parameter.getClassObjectType();
            if (classObjectType != null && JetStandardClasses.isNothing(classObjectType)) {
                PsiElement nameIdentifier = typeParameters.get(parameter.getIndex()).getNameIdentifier();
                if (nameIdentifier != null) {
//                    trace.getErrorHandler().genericError(nameIdentifier.getNode(), "Class object upper bounds of " + parameter.getName() + " have empty intersection");
                    trace.report(CONFLICTING_CLASS_OBJECT_UPPER_BOUNDS.on(nameIdentifier, parameter));
                }
            }
        }
    }

    private JetType resolveAndCheckUpperBoundType(@NotNull JetTypeReference upperBound, @NotNull JetScope scope, boolean classObjectConstaint) {
        JetType jetType = typeResolverNotCheckingBounds.resolveType(scope, upperBound);
        if (!TypeUtils.canHaveSubtypes(semanticServices.getTypeChecker(), jetType)) {
            if (classObjectConstaint) {
//                trace.getErrorHandler().genericError(upperBound.getNode(), jetType + " is a final type, and thus a class object cannot extend it");
                trace.report(FINAL_CLASS_OBJECT_UPPER_BOUND.on(upperBound, jetType));
            }
            else {
//                trace.getErrorHandler().genericWarning(upperBound.getNode(), jetType + " is a final type, and thus a value of the type parameter is predetermined");
                trace.report(FINAL_UPPER_BOUND.on(upperBound, jetType));
            }
        }
        return jetType;
    }

    @NotNull
    public VariableDescriptor resolveLocalVariableDescriptor(@NotNull DeclarationDescriptor containingDeclaration, @NotNull JetScope scope, @NotNull JetParameter parameter) {
        JetType type = resolveParameterType(scope, parameter);
        return resolveLocalVariableDescriptor(containingDeclaration, parameter, type);
    }

    private JetType resolveParameterType(JetScope scope, JetParameter parameter) {
        JetTypeReference typeReference = parameter.getTypeReference();
        JetType type;
        if (typeReference != null) {
            type = typeResolver.resolveType(scope, typeReference);
        }
        else {
            // Error is reported by the parser
            type = ErrorUtils.createErrorType("Annotation is absent");
        }
        return type;
    }

    public VariableDescriptor resolveLocalVariableDescriptor(@NotNull DeclarationDescriptor containingDeclaration, @NotNull JetParameter parameter, @NotNull JetType type) {
        VariableDescriptor variableDescriptor = new LocalVariableDescriptor(
                containingDeclaration,
                annotationResolver.createAnnotationStubs(parameter.getModifierList()),
                JetPsiUtil.safeName(parameter.getName()),
                type,
                parameter.isMutable());
        trace.record(BindingContext.VALUE_PARAMETER, parameter, variableDescriptor);
        return variableDescriptor;
    }

    @NotNull
    public VariableDescriptor resolveLocalVariableDescriptor(DeclarationDescriptor containingDeclaration, JetScope scope, JetProperty property) {
        JetType type = getVariableType(scope, property, false); // For a local variable the type must not be deferred

        return resolveLocalVariableDescriptorWithType(containingDeclaration, property, type);
    }

    @NotNull
    public VariableDescriptor resolveLocalVariableDescriptorWithType(DeclarationDescriptor containingDeclaration, JetProperty property, JetType type) {
        VariableDescriptorImpl variableDescriptor = new LocalVariableDescriptor(
                containingDeclaration,
                annotationResolver.createAnnotationStubs(property.getModifierList()),
                JetPsiUtil.safeName(property.getName()),
                type,
                property.isVar());
        trace.record(BindingContext.VARIABLE, property, variableDescriptor);
        return variableDescriptor;
    }

    public PropertyDescriptor resolveObjectDeclarationAsPropertyDescriptor(@NotNull DeclarationDescriptor containingDeclaration, JetClassOrObject objectDeclaration, @NotNull ClassDescriptor classDescriptor) {
        JetModifierList modifierList = objectDeclaration.getModifierList();
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(
                containingDeclaration,
                annotationResolver.createAnnotationStubs(modifierList),
                Modality.FINAL,
                resolveVisibilityFromModifiers(objectDeclaration.getModifierList()),
                false,
                null,
                JetPsiUtil.safeName(objectDeclaration.getName()),
                null,
                classDescriptor.getDefaultType());

        propertyDescriptor.initialize(
                Collections.<TypeParameterDescriptor>emptyList(),
                null, // TODO : is it really OK?
                null);

        JetObjectDeclarationName nameAsDeclaration = objectDeclaration.getNameAsDeclaration();
        if (nameAsDeclaration != null) {
            trace.record(BindingContext.OBJECT_DECLARATION, nameAsDeclaration, propertyDescriptor);
        }
        return propertyDescriptor;
    }

    @NotNull
    public PropertyDescriptor resolvePropertyDescriptor(@NotNull DeclarationDescriptor containingDeclaration, @NotNull JetScope scope, JetProperty property) {
        JetScope scopeWithTypeParameters;
        List<TypeParameterDescriptor> typeParameterDescriptors;
        List<JetTypeParameter> typeParameters = property.getTypeParameters();
        if (typeParameters.isEmpty()) {
            scopeWithTypeParameters = scope;
            typeParameterDescriptors = Collections.emptyList();
        }
        else {
            WritableScope writableScope = new WritableScopeImpl(scope, containingDeclaration, new TraceBasedRedeclarationHandler(trace)).setDebugName("Scope with type parameters of a property");
            typeParameterDescriptors = resolveTypeParameters(containingDeclaration, writableScope, typeParameters);
            resolveGenericBounds(property, writableScope, typeParameterDescriptors);
            scopeWithTypeParameters = writableScope;
        }

        JetType receiverType = null;
        JetTypeReference receiverTypeRef = property.getReceiverTypeRef();
        if (receiverTypeRef != null) {
            receiverType = typeResolver.resolveType(scopeWithTypeParameters, receiverTypeRef);
        }

        JetModifierList modifierList = property.getModifierList();
        boolean isVar = property.isVar();

        JetType type = getVariableType(scopeWithTypeParameters, property, true);

        boolean hasBody = property.getInitializer() != null;
        if (!hasBody) {
            JetPropertyAccessor getter = property.getGetter();
            if (getter != null && getter.getBodyExpression() != null) {
                hasBody = true;
            }
            JetPropertyAccessor setter = property.getSetter();
            if (!hasBody && setter != null && setter.getBodyExpression() != null) {
                hasBody = true;
            }
        }
        Modality defaultModality = getDefaultModality(containingDeclaration, hasBody);
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(
                containingDeclaration,
                annotationResolver.resolveAnnotations(scope, modifierList),
                resolveModalityFromModifiers(property.getModifierList(), defaultModality),
                resolveVisibilityFromModifiers(property.getModifierList()),
                isVar,
                receiverType,
                JetPsiUtil.safeName(property.getName()),
                isVar ? type : null,
                type);

        propertyDescriptor.initialize(
                typeParameterDescriptors,
                resolvePropertyGetterDescriptor(scopeWithTypeParameters, property, propertyDescriptor),
                resolvePropertySetterDescriptor(scopeWithTypeParameters, property, propertyDescriptor));

        trace.record(BindingContext.VARIABLE, property, propertyDescriptor);
        return propertyDescriptor;
    }

    @NotNull
    private JetType getVariableType(@NotNull final JetScope scope, @NotNull final JetProperty property, boolean allowDeferred) {
        // TODO : receiver?
        JetTypeReference propertyTypeRef = property.getPropertyTypeRef();

        if (propertyTypeRef == null) {
            final JetExpression initializer = property.getInitializer();
            if (initializer == null) {
//                trace.getErrorHandler().genericError(property.getNode(), "This property must either have a type annotation or be initialized");
                PsiElement nameIdentifier = property.getNameIdentifier();
                if (nameIdentifier != null) {
                    trace.report(PROPERTY_WITH_NO_TYPE_NO_INITIALIZER.on(nameIdentifier.getNode()));
                }
                return ErrorUtils.createErrorType("No type, no body");
            } else {
                // TODO : a risk of a memory leak
                LazyValue<JetType> lazyValue = new LazyValue<JetType>() {
                    @Override
                    protected JetType compute() {
                        JetFlowInformationProvider flowInformationProvider = computeFlowData(property, initializer);
                        return semanticServices.getTypeInferrerServices(trace, flowInformationProvider).safeGetType(scope, initializer, JetTypeInferrer.NO_EXPECTED_TYPE);
                    }
                };
                if (allowDeferred) {
                    return new DeferredType(lazyValue);
                }
                else {
                    return lazyValue.get();
                }
            }
        } else {
            return typeResolver.resolveType(scope, propertyTypeRef);
        }
    }

    @NotNull
    /*package*/ static Modality resolveModalityFromModifiers(@Nullable JetModifierList modifierList, @NotNull Modality defaultModality) {
        if (modifierList == null) return defaultModality;
        boolean hasAbstractModifier = modifierList.hasModifier(JetTokens.ABSTRACT_KEYWORD);
        boolean hasOverrideModifier = modifierList.hasModifier(JetTokens.OVERRIDE_KEYWORD);

        if (modifierList.hasModifier(JetTokens.OPEN_KEYWORD)) {
            if (hasAbstractModifier || defaultModality == Modality.ABSTRACT) {
                return Modality.ABSTRACT;
            }
            return Modality.OPEN;
        }
        if (hasAbstractModifier) {
            return Modality.ABSTRACT;
        }
        boolean hasFinalModifier = modifierList.hasModifier(JetTokens.FINAL_KEYWORD);
        if (hasOverrideModifier && !hasFinalModifier && !(defaultModality == Modality.ABSTRACT)) {
            return Modality.OPEN;
        }
        if (hasFinalModifier) {
            return Modality.FINAL;
        }
        return defaultModality;
    }
    
    @NotNull
    /*package*/ static Visibility resolveVisibilityFromModifiers(@Nullable JetModifierList modifierList) {
        return resolveVisibilityFromModifiers(modifierList, Visibility.INTERNAL);
    }

    @NotNull
    /*package*/ static Visibility resolveVisibilityFromModifiers(@Nullable JetModifierList modifierList, @NotNull Visibility defaultVisibility) {
        if (modifierList == null) return defaultVisibility;
        if (modifierList.hasModifier(JetTokens.PRIVATE_KEYWORD)) return Visibility.PRIVATE;
        if (modifierList.hasModifier(JetTokens.PUBLIC_KEYWORD)) return Visibility.PUBLIC;
        if (modifierList.hasModifier(JetTokens.PROTECTED_KEYWORD)) {
            if (modifierList.hasModifier(JetTokens.INTERNAL_KEYWORD)) {
                return Visibility.INTERNAL_PROTECTED;
            }
            return Visibility.PROTECTED;
        }
        return defaultVisibility;
    }

    @Nullable
    private PropertySetterDescriptor resolvePropertySetterDescriptor(@NotNull JetScope scope, @NotNull JetProperty property, @NotNull PropertyDescriptor propertyDescriptor) {
        JetPropertyAccessor setter = property.getSetter();
        PropertySetterDescriptor setterDescriptor = null;
        if (setter != null) {
            List<AnnotationDescriptor> annotations = annotationResolver.resolveAnnotations(scope, setter.getModifierList());
            JetParameter parameter = setter.getParameter();

            setterDescriptor = new PropertySetterDescriptor(
                    resolveModalityFromModifiers(setter.getModifierList(), propertyDescriptor.getModality()),
                    resolveVisibilityFromModifiers(setter.getModifierList(), propertyDescriptor.getVisibility()),
                    propertyDescriptor, annotations, setter.getBodyExpression() != null, false);
            if (parameter != null) {
                if (parameter.isRef()) {
//                    trace.getErrorHandler().genericError(parameter.getRefNode(), "Setter parameters can not be 'ref'");
                    trace.report(Errors.REF_SETTER_PARAMETER.on(parameter.getRefNode()));
                }

                // This check is redundant: the parser does not allow a default value, but we'll keep it just in case
                JetExpression defaultValue = parameter.getDefaultValue();
                if (defaultValue != null) {
//                    trace.getErrorHandler().genericError(defaultValue.getNode(), "Setter parameters can not have default values");
                    trace.report(SETTER_PARAMETER_WITH_DEFAULT_VALUE.on(defaultValue));
                }

                JetType type;
                JetTypeReference typeReference = parameter.getTypeReference();
                if (typeReference == null) {
                    type = propertyDescriptor.getInType(); // TODO : this maybe unknown at this point
                }
                else {
                    type = typeResolver.resolveType(scope, typeReference);
                    JetType inType = propertyDescriptor.getInType();
                    if (inType != null) {
                        if (!semanticServices.getTypeChecker().equalTypes(type, inType)) {
//                            trace.getErrorHandler().genericError(typeReference.getNode(), "Setter parameter type must be equal to the type of the property, i.e. " + inType);
                            trace.report(WRONG_SETTER_PARAMETER_TYPE.on(setter, typeReference, inType));
                        }
                    }
                    else {
                        // TODO : the same check may be needed later???
                    }
                }

                MutableValueParameterDescriptor valueParameterDescriptor = resolveValueParameterDescriptor(setterDescriptor, parameter, 0, type);
                setterDescriptor.initialize(valueParameterDescriptor);
            }
            trace.record(BindingContext.PROPERTY_ACCESSOR, setter, setterDescriptor);
        }
        else if (property.isVar()) {
            setterDescriptor = new PropertySetterDescriptor(
                    propertyDescriptor.getModality(),
                    propertyDescriptor.getVisibility(),
                    propertyDescriptor, Collections.<AnnotationDescriptor>emptyList(), false, true);
        }

        if (! property.isVar()) {
            if (setter != null) {
//                trace.getErrorHandler().genericError(setter.asElement().getNode(), "A 'val'-property cannot have a setter");
                trace.report(VAL_WITH_SETTER.on(property, setter));
            }
        }
        return setterDescriptor;
    }

    @Nullable
    private PropertyGetterDescriptor resolvePropertyGetterDescriptor(@NotNull JetScope scope, @NotNull JetProperty property, @NotNull PropertyDescriptor propertyDescriptor) {
        PropertyGetterDescriptor getterDescriptor;
        JetPropertyAccessor getter = property.getGetter();
        if (getter != null) {
            List<AnnotationDescriptor> annotations = annotationResolver.resolveAnnotations(scope, getter.getModifierList());

            JetType outType = propertyDescriptor.getOutType();
            JetType returnType = outType;
            JetTypeReference returnTypeReference = getter.getReturnTypeReference();
            if (returnTypeReference != null) {
                returnType = typeResolver.resolveType(scope, returnTypeReference);
                if (outType != null && !semanticServices.getTypeChecker().equalTypes(returnType, outType)) {
//                    trace.getErrorHandler().genericError(returnTypeReference.getNode(), "Getter return type must be equal to the type of the property, i.e. " + propertyDescriptor.getReturnType());
                    trace.report(WRONG_GETTER_RETURN_TYPE.on(getter, returnTypeReference, propertyDescriptor.getReturnType()));
                }
            }

            getterDescriptor = new PropertyGetterDescriptor(
                    propertyDescriptor, annotations, resolveModalityFromModifiers(getter.getModifierList(), propertyDescriptor.getModality()),
                    resolveVisibilityFromModifiers(getter.getModifierList(), propertyDescriptor.getVisibility()),
                    returnType, getter.getBodyExpression() != null, false);
            trace.record(BindingContext.PROPERTY_ACCESSOR, getter, getterDescriptor);
        }
        else {
            getterDescriptor = new PropertyGetterDescriptor(
                    propertyDescriptor, Collections.<AnnotationDescriptor>emptyList(), propertyDescriptor.getModality(),
                    propertyDescriptor.getVisibility(),
                    propertyDescriptor.getOutType(), false, true);
        }
        return getterDescriptor;
    }

    @NotNull
    public ConstructorDescriptorImpl resolveSecondaryConstructorDescriptor(@NotNull JetScope scope, @NotNull ClassDescriptor classDescriptor, @NotNull JetConstructor constructor) {
        return createConstructorDescriptor(scope, classDescriptor, false, constructor.getModifierList(), constructor, classDescriptor.getTypeConstructor().getParameters(), constructor.getParameters());
    }

    @NotNull
    private ConstructorDescriptorImpl createConstructorDescriptor(
            @NotNull JetScope scope,
            @NotNull ClassDescriptor classDescriptor,
            boolean isPrimary,
            @Nullable JetModifierList modifierList,
            @NotNull JetDeclaration declarationToTrace,
            List<TypeParameterDescriptor> typeParameters, @NotNull List<JetParameter> valueParameters) {
        ConstructorDescriptorImpl constructorDescriptor = new ConstructorDescriptorImpl(
                classDescriptor,
                annotationResolver.resolveAnnotations(scope, modifierList),
                isPrimary
        );
        trace.record(BindingContext.CONSTRUCTOR, declarationToTrace, constructorDescriptor);
        return constructorDescriptor.initialize(
                typeParameters,
                resolveValueParameters(
                        constructorDescriptor,
                        new WritableScopeImpl(scope, classDescriptor, new TraceBasedRedeclarationHandler(trace)).setDebugName("Scope with value parameters of a constructor"),
                        valueParameters),
                        Modality.FINAL,
                        resolveVisibilityFromModifiers(modifierList));
    }

    @Nullable
    public ConstructorDescriptorImpl resolvePrimaryConstructorDescriptor(@NotNull JetScope scope, @NotNull ClassDescriptor classDescriptor, @NotNull JetClass classElement) {
        if (!classElement.hasPrimaryConstructor()) return null;
        return createConstructorDescriptor(
                scope,
                classDescriptor,
                true,
                classElement.getPrimaryConstructorModifierList(),
                classElement,
                classDescriptor.getTypeConstructor().getParameters(), classElement.getPrimaryConstructorParameters());
    }

    @NotNull
    public PropertyDescriptor resolvePrimaryConstructorParameterToAProperty(
            @NotNull ClassDescriptor classDescriptor,
            @NotNull JetScope scope,
            @NotNull JetParameter parameter) {
        JetType type = resolveParameterType(scope, parameter);
        String name = parameter.getName();
        boolean isMutable = parameter.isMutable();
        JetModifierList modifierList = parameter.getModifierList();

        if (modifierList != null) {
            ASTNode abstractNode = modifierList.getModifierNode(JetTokens.ABSTRACT_KEYWORD);
            if (abstractNode != null) {
//                trace.getErrorHandler().genericError(abstractNode, "This property cannot be declared abstract");
                trace.report(ABSTRACT_PROPERTY_IN_PRIMARY_CONSTRUCTOR_PARAMETERS.on(parameter, abstractNode));
            }
        }

        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(
                classDescriptor,
                annotationResolver.resolveAnnotations(scope, modifierList),
                resolveModalityFromModifiers(parameter.getModifierList(), Modality.FINAL),
                resolveVisibilityFromModifiers(parameter.getModifierList()),
                isMutable,
                null,
                name == null ? "<no name>" : name,
                isMutable ? type : null,
                type);
        propertyDescriptor.initialize(Collections.<TypeParameterDescriptor>emptyList(), null, null);
        trace.record(BindingContext.PRIMARY_CONSTRUCTOR_PARAMETER, parameter, propertyDescriptor);
        return propertyDescriptor;
    }

    public void checkBounds(@NotNull JetTypeReference typeReference, @NotNull JetType type) {
        if (ErrorUtils.isErrorType(type)) return;

        JetTypeElement typeElement = typeReference.getTypeElement();
        if (typeElement == null) return;

        List<TypeParameterDescriptor> parameters = type.getConstructor().getParameters();
        List<TypeProjection> arguments = type.getArguments();
        assert parameters.size() == arguments.size();

        List<JetTypeReference> typeReferences = typeElement.getTypeArgumentsAsTypes();
        assert typeReferences.size() == arguments.size() : typeElement.getText();

        TypeSubstitutor substitutor = TypeSubstitutor.create(type);
        for (int i = 0, projectionsSize = typeReferences.size(); i < projectionsSize; i++) {
            JetTypeReference argumentTypeReference = typeReferences.get(i);

            if (argumentTypeReference == null) continue;

            JetType typeArgument = arguments.get(i).getType();
            checkBounds(argumentTypeReference, typeArgument);

            TypeParameterDescriptor typeParameterDescriptor = parameters.get(i);
            checkBounds(argumentTypeReference, typeArgument, typeParameterDescriptor, substitutor);
        }
    }

    public void checkBounds(
            @NotNull JetTypeReference argumentTypeReference,
            @NotNull JetType typeArgument,
            @NotNull TypeParameterDescriptor typeParameterDescriptor,
            @NotNull TypeSubstitutor substitutor) {
        for (JetType bound : typeParameterDescriptor.getUpperBounds()) {
            JetType substitutedBound = substitutor.safeSubstitute(bound, Variance.INVARIANT);
            if (!semanticServices.getTypeChecker().isSubtypeOf(typeArgument, substitutedBound)) {
//                trace.getErrorHandler().genericError(argumentTypeReference.getNode(), "An upper bound " + substitutedBound + " is violated");
                trace.report(UPPER_BOUND_VIOLATED.on(argumentTypeReference, substitutedBound));
            }
        }
    }

    public JetFlowInformationProvider computeFlowData(@NotNull JetElement declaration, @NotNull final JetExpression bodyExpression) {
        final JetPseudocodeTrace pseudocodeTrace = flowDataTraceFactory.createTrace(declaration);
        final Map<JetElement, Pseudocode> pseudocodeMap = new HashMap<JetElement, Pseudocode>();
        final Map<JetElement, Instruction> representativeInstructions = new HashMap<JetElement, Instruction>();
        final Map<JetExpression, LoopInfo> loopInfo = Maps.newHashMap();
        JetPseudocodeTrace wrappedTrace = new JetPseudocodeTrace() {
            @Override
            public void recordControlFlowData(@NotNull JetElement element, @NotNull Pseudocode pseudocode) {
                pseudocodeTrace.recordControlFlowData(element, pseudocode);
                pseudocodeMap.put(element, pseudocode);
            }

            @Override
            public void recordRepresentativeInstruction(@NotNull JetElement element, @NotNull Instruction instruction) {
                Instruction oldValue = representativeInstructions.put(element, instruction);
//                assert oldValue == null : element.getText();
                pseudocodeTrace.recordRepresentativeInstruction(element, instruction);
            }

            @Override
            public void recordLoopInfo(JetExpression expression, LoopInfo blockInfo) {
                loopInfo.put(expression, blockInfo);
                pseudocodeTrace.recordLoopInfo(expression, blockInfo);
            }

            @Override
            public void close() {
                pseudocodeTrace.close();
                for (Pseudocode pseudocode : pseudocodeMap.values()) {
                    pseudocode.postProcess();
                }
            }
        };
        JetControlFlowInstructionsGenerator instructionsGenerator = new JetControlFlowInstructionsGenerator(wrappedTrace);
        new JetControlFlowProcessor(trace, instructionsGenerator).generate(declaration, bodyExpression);
        wrappedTrace.close();
        return new JetFlowInformationProvider() {
            @Override
            public void collectReturnedInformation(@NotNull JetElement subroutine, @NotNull Collection<JetExpression> returnedExpressions, @NotNull Collection<JetElement> elementsReturningUnit) {
                Pseudocode pseudocode = pseudocodeMap.get(subroutine);
                assert pseudocode != null;

                SubroutineExitInstruction exitInstruction = pseudocode.getExitInstruction();
                processPreviousInstructions(exitInstruction, new HashSet<Instruction>(), returnedExpressions, elementsReturningUnit);
            }

            @Override
            public void collectReturnExpressions(@NotNull JetElement subroutine, @NotNull final Collection<JetExpression> returnedExpressions) {
                Pseudocode pseudocode = pseudocodeMap.get(subroutine);
                assert pseudocode != null;

                SubroutineExitInstruction exitInstruction = pseudocode.getExitInstruction();
                for (Instruction previousInstruction : exitInstruction.getPreviousInstructions()) {
                    previousInstruction.accept(new InstructionVisitor() {
                        @Override
                        public void visitReturnValue(ReturnValueInstruction instruction) {
                            returnedExpressions.add((JetExpression) instruction.getElement());
                        }

                        @Override
                        public void visitReturnNoValue(ReturnNoValueInstruction instruction) {
                            returnedExpressions.add((JetExpression) instruction.getElement());
                        }



                        @Override
                        public void visitJump(AbstractJumpInstruction instruction) {
                            // Nothing
                        }

                        @Override
                        public void visitInstruction(Instruction instruction) {
                            if (instruction instanceof JetElementInstruction) {
                                JetElementInstruction elementInstruction = (JetElementInstruction) instruction;
                                returnedExpressions.add((JetExpression) elementInstruction.getElement());
                            }
                            else {
                                throw new IllegalStateException(instruction + " precedes the exit point");
                            }
                        }
                    });
                }
            }

            @Override
            public void collectUnreachableExpressions(@NotNull JetElement subroutine, @NotNull Collection<JetElement> unreachableElements) {
                Pseudocode pseudocode = pseudocodeMap.get(subroutine);
                assert pseudocode != null;

                SubroutineEnterInstruction enterInstruction = pseudocode.getEnterInstruction();
                Set<Instruction> visited = new HashSet<Instruction>();
                collectReachable(enterInstruction, visited, null);

                for (Instruction instruction : pseudocode.getInstructions()) {
                    if (!visited.contains(instruction) &&
                        instruction instanceof JetElementInstruction &&
                        // TODO : do {return} while (1 > a)
                        !(instruction instanceof ReadUnitValueInstruction)) {
                        unreachableElements.add(((JetElementInstruction) instruction).getElement());
                    }
                }
            }

            @Override
            public void collectDominatedExpressions(@NotNull JetExpression dominator, @NotNull Collection<JetElement> dominated) {
                Instruction dominatorInstruction = representativeInstructions.get(dominator);
                if (dominatorInstruction == null) {
                    return;
                }
                SubroutineEnterInstruction enterInstruction = dominatorInstruction.getOwner().getEnterInstruction();

                Set<Instruction> reachable = new HashSet<Instruction>();
                collectReachable(enterInstruction, reachable, null);

                Set<Instruction> reachableWithDominatorProhibited = new HashSet<Instruction>();
                reachableWithDominatorProhibited.add(dominatorInstruction);
                collectReachable(enterInstruction, reachableWithDominatorProhibited, null);

                for (Instruction instruction : reachable) {
                    if (instruction instanceof JetElementInstruction
                            && reachable.contains(instruction)
                            && !reachableWithDominatorProhibited.contains(instruction)) {
                        JetElementInstruction elementInstruction = (JetElementInstruction) instruction;
                        dominated.add(elementInstruction.getElement());
                    }
                }
            }

            @Override
            public boolean isBreakable(JetLoopExpression loop) {
                LoopInfo info = loopInfo.get(loop);
                Pseudocode.PseudocodeLabel bodyEntryPoint = (Pseudocode.PseudocodeLabel) info.getBodyEntryPoint();
                Pseudocode.PseudocodeLabel exitPoint = (Pseudocode.PseudocodeLabel) info.getExitPoint();
                HashSet<Instruction> visited = Sets.newHashSet();
                Pseudocode.PseudocodeLabel conditionEntryPoint = (Pseudocode.PseudocodeLabel) info.getConditionEntryPoint();
                visited.add(conditionEntryPoint.resolveToInstruction());
                return collectReachable(bodyEntryPoint.resolveToInstruction(), visited, exitPoint.resolveToInstruction());
            }

            public boolean isReachable(JetExpression from, JetExpression to) {
                Instruction fromInstr = representativeInstructions.get(from);
                assert fromInstr != null : "No representative instruction for " + from.getText();
                Instruction toInstr = representativeInstructions.get(to);
                assert toInstr != null : "No representative instruction for " + to.getText();

                return collectReachable(fromInstr, Sets.<Instruction>newHashSet(), toInstr);
            }
        };
    }

    private boolean collectReachable(Instruction current, Set<Instruction> visited, @Nullable Instruction lookFor) {
        if (!visited.add(current)) return false;
        if (current == lookFor) return true;

        for (Instruction nextInstruction : current.getNextInstructions()) {
            if (collectReachable(nextInstruction, visited, lookFor)) {
                return true;
            }
        }
        return false;
    }

    private void processPreviousInstructions(Instruction previousFor, final Set<Instruction> visited, final Collection<JetExpression> returnedExpressions, final Collection<JetElement> elementsReturningUnit) {
        if (!visited.add(previousFor)) return;

        Collection<Instruction> previousInstructions = previousFor.getPreviousInstructions();
        InstructionVisitor visitor = new InstructionVisitor() {
            @Override
            public void visitReadValue(ReadValueInstruction instruction) {
                returnedExpressions.add((JetExpression) instruction.getElement());
            }

            @Override
            public void visitReturnValue(ReturnValueInstruction instruction) {
                processPreviousInstructions(instruction, visited, returnedExpressions, elementsReturningUnit);
            }

            @Override
            public void visitReturnNoValue(ReturnNoValueInstruction instruction) {
                elementsReturningUnit.add(instruction.getElement());
            }

            @Override
            public void visitSubroutineEnter(SubroutineEnterInstruction instruction) {
                elementsReturningUnit.add(instruction.getSubroutine());
            }

            @Override
            public void visitUnsupportedElementInstruction(UnsupportedElementInstruction instruction) {
//                trace.getErrorHandler().genericError(instruction.getElement().getNode(), "Unsupported by control-flow builder " + instruction.getElement());
                trace.report(UNSUPPORTED.on(instruction.getElement(), "Control-flow builder"));
            }

            @Override
            public void visitWriteValue(WriteValueInstruction writeValueInstruction) {
                elementsReturningUnit.add(writeValueInstruction.getElement());
            }

            @Override
            public void visitJump(AbstractJumpInstruction instruction) {
                processPreviousInstructions(instruction, visited, returnedExpressions, elementsReturningUnit);
            }

            @Override
            public void visitReadUnitValue(ReadUnitValueInstruction instruction) {
                returnedExpressions.add((JetExpression) instruction.getElement());
            }

            @Override
            public void visitInstruction(Instruction instruction) {
                if (instruction instanceof JetElementInstructionImpl) {
                    JetElementInstructionImpl elementInstruction = (JetElementInstructionImpl) instruction;
//                    trace.getErrorHandler().genericError(elementInstruction.getElement().getNode(), "Unsupported by control-flow builder " + elementInstruction.getElement());
                    trace.report(UNSUPPORTED.on(elementInstruction.getElement(), "Control-flow builder"));
                }
                else {
                    throw new UnsupportedOperationException(instruction.toString());
                }
            }
        };
        for (Instruction previousInstruction : previousInstructions) {
            previousInstruction.accept(visitor);
        }
    }
}
