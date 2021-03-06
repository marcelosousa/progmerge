/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.findUsages;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Ordering;
import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.codeInsight.TargetElementUtilBase;
import com.intellij.find.FindManager;
import com.intellij.find.findUsages.*;
import com.intellij.find.impl.FindManagerImpl;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.UsageGroup;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.intellij.usages.UsageViewPresentation;
import com.intellij.usages.impl.rules.UsageType;
import com.intellij.usages.impl.rules.UsageTypeProvider;
import com.intellij.usages.rules.UsageFilteringRule;
import com.intellij.usages.rules.UsageGroupingRule;
import com.intellij.util.ArrayUtil;
import com.intellij.util.CommonProcessors;
import kotlin.KotlinPackage;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.findUsages.KotlinClassFindUsagesOptions;
import org.jetbrains.kotlin.idea.findUsages.KotlinFunctionFindUsagesOptions;
import org.jetbrains.kotlin.idea.findUsages.KotlinPropertyFindUsagesOptions;
import org.jetbrains.kotlin.idea.test.JetLightCodeInsightFixtureTestCase;
import org.jetbrains.kotlin.idea.test.JetWithJdkAndRuntimeLightProjectDescriptor;
import org.jetbrains.kotlin.idea.test.PluginTestCaseBase;
import org.jetbrains.kotlin.idea.util.ProjectRootsUtil;
import org.jetbrains.kotlin.psi.*;
import org.jetbrains.kotlin.test.InTextDirectivesUtils;
import org.jetbrains.kotlin.test.JetTestUtils;
import org.testng.internal.PropertiesFile;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class AbstractJetFindUsagesTest extends JetLightCodeInsightFixtureTestCase {

    public static final UsageViewPresentation USAGE_VIEW_PRESENTATION = new UsageViewPresentation();

    protected enum OptionsParser {
        CLASS {
            @NotNull
            @Override
            public FindUsagesOptions parse(@NotNull String text, @NotNull Project project) {
                KotlinClassFindUsagesOptions options = new KotlinClassFindUsagesOptions(project);
                options.isUsages = false;
                options.isSearchForTextOccurrences = false;
                options.setSearchConstructorUsages(false);
                for (String s : InTextDirectivesUtils.findListWithPrefixes(text, "// OPTIONS: ")) {
                    if (parseCommonOptions(options, s)) continue;

                    if (s.equals("constructorUsages")) {
                        options.setSearchConstructorUsages(true);
                    }
                    else if (s.equals("derivedInterfaces")) {
                        options.isDerivedInterfaces = true;
                    }
                    else if (s.equals("derivedClasses")) {
                        options.isDerivedClasses = true;
                    }
                    else if (s.equals("functionUsages")) {
                        options.isMethodsUsages = true;
                    }
                    else if (s.equals("propertyUsages")) {
                        options.isFieldsUsages = true;
                    }
                    else fail("Invalid option: " + s);
                }

                return options;
            }
        },
        FUNCTION {
            @NotNull
            @Override
            public FindUsagesOptions parse(@NotNull String text, @NotNull Project project) {
                KotlinFunctionFindUsagesOptions options = new KotlinFunctionFindUsagesOptions(project);
                options.isUsages = false;
                for (String s : InTextDirectivesUtils.findListWithPrefixes(text, "// OPTIONS: ")) {
                    if (parseCommonOptions(options, s)) continue;

                    if (s.equals("overrides")) {
                        options.isOverridingMethods = true;
                        options.isImplementingMethods = true;
                    }
                    else if (s.equals("overloadUsages")) {
                        options.isIncludeOverloadUsages = true;
                        options.isUsages = true;
                    }
                    else fail("Invalid option: " + s);
                }

                return options;
            }
        },
        PROPERTY {
            @NotNull
            @Override
            public FindUsagesOptions parse(@NotNull String text, @NotNull Project project) {
                KotlinPropertyFindUsagesOptions options = new KotlinPropertyFindUsagesOptions(project);
                options.isUsages = false;
                for (String s : InTextDirectivesUtils.findListWithPrefixes(text, "// OPTIONS: ")) {
                    if (parseCommonOptions(options, s)) continue;

                    if (s.equals("overrides")) {
                        options.setSearchOverrides(true);
                    }
                    else if (s.equals("skipRead")) {
                        options.isReadAccess = false;
                    }
                    else if (s.equals("skipWrite")) {
                        options.isWriteAccess = false;
                    }
                    else fail("Invalid option: " + s);
                }

                return options;
            }
        },
        JAVA_CLASS {
            @NotNull
            @Override
            public FindUsagesOptions parse(@NotNull String text, @NotNull Project project) {
                KotlinClassFindUsagesOptions options = new KotlinClassFindUsagesOptions(project);
                options.isUsages = false;
                options.setSearchConstructorUsages(false);
                for (String s : InTextDirectivesUtils.findListWithPrefixes(text, "// OPTIONS: ")) {
                    if (parseCommonOptions(options, s)) continue;

                    if (s.equals("derivedInterfaces")) {
                        options.isDerivedInterfaces = true;
                    }
                    else if (s.equals("derivedClasses")) {
                        options.isDerivedClasses = true;
                    }
                    else if (s.equals("implementingClasses")) {
                        options.isImplementingClasses = true;
                    }
                    else if (s.equals("methodUsages")) {
                        options.isMethodsUsages = true;
                    }
                    else if (s.equals("fieldUsages")) {
                        options.isFieldsUsages = true;
                    }
                    else fail("Invalid option: " + s);
                }

                return options;
            }
        },
        JAVA_METHOD {
            @NotNull
            @Override
            public FindUsagesOptions parse(@NotNull String text, @NotNull Project project) {
                JavaMethodFindUsagesOptions options = new JavaMethodFindUsagesOptions(project);
                options.isUsages = false;
                for (String s : InTextDirectivesUtils.findListWithPrefixes(text, "// OPTIONS: ")) {
                    if (parseCommonOptions(options, s)) continue;

                    if (s.equals("overrides")) {
                        options.isOverridingMethods = true;
                        options.isImplementingMethods = true;
                    }
                    else fail("Invalid option: " + s);
                }

                return options;
            }
        },
        JAVA_FIELD {
            @NotNull
            @Override
            public FindUsagesOptions parse(@NotNull String text, @NotNull Project project) {
                return new JavaVariableFindUsagesOptions(project);
            }
        },
        JAVA_PACKAGE {
            @NotNull
            @Override
            public FindUsagesOptions parse(@NotNull String text, @NotNull Project project) {
                return new JavaPackageFindUsagesOptions(project);
            }
        },
        DEFAULT {
            @NotNull
            @Override
            public FindUsagesOptions parse(@NotNull String text, @NotNull Project project) {
                return new FindUsagesOptions(project);
            }
        };

        protected static boolean parseCommonOptions(JavaFindUsagesOptions options, String s) {
            if (s.equals("usages")) {
                options.isUsages = true;
                return true;
            }

            if (s.equals("skipImports")) {
                options.isSkipImportStatements = true;
                return true;
            }

            else if (s.equals("textOccurrences")) {
                options.isSearchForTextOccurrences = true;
                return true;
            }

            return false;
        }

        @NotNull
        public abstract FindUsagesOptions parse(@NotNull String text, @NotNull Project project);

        @Nullable
        public static OptionsParser getParserByPsiElementClass(@NotNull Class<? extends PsiElement> klass) {
            if (klass == JetNamedFunction.class) {
                return FUNCTION;
            }
            if (klass == JetProperty.class || klass == JetParameter.class) {
                return PROPERTY;
            }
            if (klass == JetClass.class) {
                return CLASS;
            }
            if (klass == PsiMethod.class) {
                return JAVA_METHOD;
            }
            if (klass == PsiClass.class) {
                return JAVA_CLASS;
            }
            if (klass == PsiField.class) {
                return JAVA_FIELD;
            }
            if (klass == PsiPackage.class) {
                return JAVA_PACKAGE;
            }
            if (klass == JetTypeParameter.class) {
                return DEFAULT;
            }

            return null;
        }
    }

    @NotNull
    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return JetWithJdkAndRuntimeLightProjectDescriptor.INSTANCE;
    }

    @Override
    public void setUp() {
        super.setUp();
        myFixture.setTestDataPath(PluginTestCaseBase.getTestDataPathBase() + "/findUsages");
    }

    protected <T extends PsiElement> void doTest(@NotNull String path) throws Exception {
        File mainFile = new File(path);
        final String mainFileName = mainFile.getName();
        String mainFileText = FileUtil.loadFile(mainFile, true);
        final String prefix = mainFileName.substring(0, mainFileName.indexOf('.') + 1);

        boolean isPropertiesFile = FileUtilRt.getExtension(path).equals("properties");

        Class<T> caretElementClass;
        if (!isPropertiesFile) {
            List<String> caretElementClassNames = InTextDirectivesUtils.findLinesWithPrefixesRemoved(mainFileText, "// PSI_ELEMENT: ");
            assert caretElementClassNames.size() == 1;
            //noinspection unchecked
            caretElementClass = (Class<T>)Class.forName(caretElementClassNames.get(0));
        }
        else {
            //noinspection unchecked
            caretElementClass = (Class<T>) (InTextDirectivesUtils.isDirectiveDefined(mainFileText, "## FIND_FILE_USAGES")
                                            ? PropertiesFile.class
                                            : Property.class);
        }

        OptionsParser parser = OptionsParser.getParserByPsiElementClass(caretElementClass);

        String rootPath = path.substring(0, path.lastIndexOf("/") + 1);

        File rootDir = new File(rootPath);
        File[] extraFiles = rootDir.listFiles(
                new FilenameFilter() {
                    @Override
                    public boolean accept(@NotNull File dir, @NotNull String name) {
                        if (!name.startsWith(prefix) || name.equals(mainFileName)) return false;

                        String ext = FileUtilRt.getExtension(name);
                        return ext.equals("kt")
                               || ext.equals("java")
                               || ext.equals("xml")
                               || ext.equals("properties")
                               || (ext.equals("txt") && !name.endsWith(".results.txt"));
                    }
                }
        );

        assert extraFiles != null;
        for (File file : extraFiles) {
            myFixture.configureByFile(rootPath + file.getName());
        }
        myFixture.configureByFile(path);

        PsiElement caretElement =
                InTextDirectivesUtils.isDirectiveDefined(mainFileText, "// FIND_BY_REF")
                ? TargetElementUtilBase.findTargetElement(myFixture.getEditor(),
                                                          TargetElementUtilBase.REFERENCED_ELEMENT_ACCEPTED |
                                                          TargetElementUtil.NEW_AS_CONSTRUCTOR)
                : myFixture.getElementAtCaret();
        assertNotNull(caretElement);
        assertInstanceOf(caretElement, caretElementClass);

        PsiFile containingFile = caretElement.getContainingFile();
        boolean isLibraryElement = containingFile != null && ProjectRootsUtil.isLibraryFile(getProject(), containingFile.getVirtualFile());

        FindUsagesOptions options = parser != null ? parser.parse(mainFileText, getProject()) : null;

        // Ensure that search by sources (if present) and decompiled declarations gives the same results
        if (isLibraryElement) {
            PsiElement originalElement = caretElement.getOriginalElement();
            findUsagesAndCheckResults(mainFileText, prefix, rootPath, originalElement, options);

            PsiElement navigationElement = caretElement.getNavigationElement();
            if (navigationElement != originalElement) {
                findUsagesAndCheckResults(mainFileText, prefix, rootPath, navigationElement, options);
            }
        }
        else {
            findUsagesAndCheckResults(mainFileText, prefix, rootPath, caretElement, options);
        }
    }

    private <T extends PsiElement> void findUsagesAndCheckResults(
            @NotNull String mainFileText,
            @NotNull String prefix,
            @NotNull String rootPath,
            @NotNull T caretElement,
            @Nullable FindUsagesOptions options
    ) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Collection<UsageInfo> usageInfos = findUsages(caretElement, options);

        Collection<UsageFilteringRule> filteringRules = instantiateClasses(mainFileText, "// FILTERING_RULES: ");
        final Collection<UsageGroupingRule> groupingRules = instantiateClasses(mainFileText, "// GROUPING_RULES: ");

        Collection<UsageInfo2UsageAdapter> filteredUsages = getUsageAdapters(filteringRules, usageInfos);

        List<String> usageFiles = KotlinPackage.distinct(
                KotlinPackage.map(
                        filteredUsages,
                        new Function1<UsageInfo2UsageAdapter, String>() {
                            @Override
                            public String invoke(UsageInfo2UsageAdapter adapter) {
                                return adapter.getFile().getName();
                            }
                        }
                )
        );
        final boolean appendFileName = usageFiles.size() > 1;

        Function<UsageInfo2UsageAdapter, String> convertToString = new Function<UsageInfo2UsageAdapter, String>() {
            @Override
            public String apply(@Nullable final UsageInfo2UsageAdapter usageAdapter) {
                assert usageAdapter != null;

                String groupAsString = Joiner.on(", ").join(
                        KotlinPackage.map(
                                groupingRules,
                                new Function1<UsageGroupingRule, String>() {
                                    @Override
                                    public String invoke(UsageGroupingRule rule) {
                                        UsageGroup group = rule.groupUsage(usageAdapter);
                                        return group != null ? group.getText(null) : "";
                                    }
                                }
                        )
                );
                if (!groupAsString.isEmpty()) {
                    groupAsString = "(" + groupAsString + ") ";
                }

                UsageType usageType = getUsageType(usageAdapter.getElement());
                String usageTypeAsString = usageType == null ? "null" : usageType.toString(USAGE_VIEW_PRESENTATION);

                return (appendFileName ? "[" + usageAdapter.getFile().getName() + "] " : "") +
                       usageTypeAsString + " " +
                       groupAsString +
                       Joiner.on("").join(Arrays.asList(usageAdapter.getPresentation().getText()));
            }
        };

        Collection<String> finalUsages = Ordering.natural().sortedCopy(Collections2.transform(filteredUsages, convertToString));
        JetTestUtils.assertEqualsToFile(new File(rootPath, prefix + "results.txt"), StringUtil.join(finalUsages, "\n"));
    }

    protected Collection<UsageInfo> findUsages(@NotNull PsiElement targetElement, @Nullable FindUsagesOptions options) {
        Project project = getProject();
        FindUsagesHandler handler =
                ((FindManagerImpl) FindManager.getInstance(project)).getFindUsagesManager().getFindUsagesHandler(targetElement, false);
        assert handler != null : "Cannot find handler for: " + targetElement;

        if (options == null) {
            options = handler.getFindUsagesOptions(null);
        }

        options.searchScope = GlobalSearchScope.allScope(project);

        CommonProcessors.CollectProcessor<UsageInfo> processor = new CommonProcessors.CollectProcessor<UsageInfo>();
        PsiElement[] psiElements = ArrayUtil.mergeArrays(handler.getPrimaryElements(), handler.getSecondaryElements());

        for (PsiElement psiElement : psiElements) {
            handler.processElementUsages(psiElement, processor, options);
        }

        return processor.getResults();
    }

    private static Collection<UsageInfo2UsageAdapter> getUsageAdapters(
            final Collection<? extends UsageFilteringRule> filters,
            Collection<UsageInfo> usageInfos
    ) {
        return Collections2.filter(
                Collections2.transform(usageInfos, new Function<UsageInfo, UsageInfo2UsageAdapter>() {
                    @Override
                    public UsageInfo2UsageAdapter apply(@Nullable UsageInfo usageInfo) {
                        assert (usageInfo != null);

                        UsageInfo2UsageAdapter usageAdapter = new UsageInfo2UsageAdapter(usageInfo);
                        for (UsageFilteringRule filter : filters) {
                            if (!filter.isVisible(usageAdapter)) {
                                return null;
                            }
                        }

                        return usageAdapter;
                    }
                }),
                Predicates.notNull());
    }

    @Nullable
    private static UsageType getUsageType(@Nullable PsiElement element) {
        if (element == null) return null;

        if (PsiTreeUtil.getParentOfType(element, PsiComment.class, false) != null) {
            return UsageType.COMMENT_USAGE;
        }

        UsageTypeProvider[] providers = Extensions.getExtensions(UsageTypeProvider.EP_NAME);
        for (UsageTypeProvider provider : providers) {
            UsageType usageType = provider.getUsageType(element);
            if (usageType != null) {
                return usageType;
            }
        }

        return UsageType.UNCLASSIFIED;
    }

    private static <T> Collection<T> instantiateClasses(String mainFileText, String directive)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        List<String> filteringRuleClassNames = InTextDirectivesUtils.findLinesWithPrefixesRemoved(mainFileText, directive);

        Collection<T> filters = new ArrayList<T>();
        for (String filteringRuleClassName : filteringRuleClassNames) {
            //noinspection unchecked
            Class<T> klass = (Class<T>) Class.forName(filteringRuleClassName);
            filters.add(klass.newInstance());
        }

        return filters;
    }
}
