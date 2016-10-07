{-# LANGUAGE DeriveDataTypeable #-}
-------------------------------------------------------------------------------
-- Module    :  Edit.Gen
-- Copyright :  (c) 2016 Marcelo Sousa
-------------------------------------------------------------------------------
module Edit.Gen where

import Language.Java.Parser hiding (opt)
import Language.Java.Pretty hiding (opt)
import Language.Java.Syntax

import Data.Map (Map)
import qualified Data.Map as M

import qualified Debug.Trace as T

import Edit.Types
import Edit.Diff

-- | Edit generation
edit_gen :: Program -> Program -> (Program, Edit, Edit)
edit_gen o_ast v_ast =
  case (o_ast, v_ast) of
    (CompilationUnit o_pkg o_imp o_ty, CompilationUnit v_pkg v_imp v_ty) ->
      let (n_tys, o_edit, v_edit) = edit_tys_gen o_ty v_ty
          no_ast = CompilationUnit o_pkg o_imp n_tys
      in (no_ast, o_edit, v_edit) 

edit_tys_gen :: [TypeDecl] -> [TypeDecl] -> ([TypeDecl], Edit, Edit)
edit_tys_gen xs ys =
  if length xs /= length ys
  then error "there are fundamental differences between the files"
  else let xys = zip xs ys
       in foldr edit_tys_gen_aux ([],[],[]) xys 

edit_tys_gen_aux :: (TypeDecl,TypeDecl) -> ([TypeDecl],Edit,Edit) -> ([TypeDecl],Edit,Edit)
edit_tys_gen_aux (ty_o,ty_v) (tys,o_edit,v_edit) =
  let (nty,o_e,v_e) = edit_ty_gen ty_o ty_v
  in (nty:tys, o_e++o_edit, v_e++v_edit)

edit_ty_gen :: TypeDecl -> TypeDecl -> (TypeDecl,Edit,Edit)
edit_ty_gen ty_o ty_v =
  case ty_o of
    ClassTypeDecl o_class -> case ty_v of
      ClassTypeDecl v_class ->
        let (no_class,o_edit,v_edit) = edit_class_gen o_class v_class
        in (ClassTypeDecl no_class,o_edit,v_edit)
      InterfaceTypeDecl v_inter -> error "unsupported differences" 
    InterfaceTypeDecl o_inter -> if ty_o == ty_v then (ty_o,[],[]) else error "unsupported differences" 

edit_class_gen :: ClassDecl -> ClassDecl -> (ClassDecl,Edit,Edit)
edit_class_gen o_class v_class =
  case (o_class,v_class) of
    (ClassDecl o_mods o_id o_tys o_mref o_reftys o_body, ClassDecl v_mods v_id v_tys v_mref v_reftys v_body) ->
      let checks = [ o_mods == v_mods
                   , o_id == v_id
                   , o_tys == v_tys
                   , o_mref == v_mref
                   , o_reftys == v_reftys ]
      in if all id checks
         then let (no_body,o_edit,v_edit) = edit_class_body_gen o_body v_body
              in (ClassDecl o_mods o_id o_tys o_mref o_reftys no_body, o_edit, v_edit)
         else error "unsupported differences"
    (EnumDecl _ _ _ _, _) -> if o_class == v_class then (o_class,[],[]) else error "unsupported differences" 

edit_class_body_gen :: ClassBody -> ClassBody -> (ClassBody,Edit,Edit)
edit_class_body_gen (ClassBody o_decls) (ClassBody v_decls) =
   if length o_decls /= length v_decls
   then error "unsupported differences"
   else let xyz = zip o_decls v_decls
            (no_decls,o_edit,v_edit) = foldr edit_class_body_gen_aux ([],[],[]) xyz 
        in (ClassBody no_decls, o_edit, v_edit)

edit_class_body_gen_aux :: (Decl,Decl) -> ([Decl],Edit,Edit) -> ([Decl],Edit,Edit)
edit_class_body_gen_aux (decl_o,decl_v) (decls,o_edit,v_edit) =
  let (ndecl,o_e,v_e) = edit_decl_gen decl_o decl_v 
  in (ndecl:decls, o_e++o_edit, v_e++v_edit)

edit_decl_gen :: Decl -> Decl -> (Decl,Edit,Edit)
edit_decl_gen o_decl v_decl =
  case (o_decl, v_decl) of
    (MemberDecl o_mem, MemberDecl v_mem) ->
      let (no_mem,o_edit,v_edit) = edit_member_gen o_mem v_mem
      in (MemberDecl no_mem, o_edit, v_edit)
    (InitDecl o_b o_block, InitDecl v_b v_block) ->
      let (no_block,o_edit,v_edit) = edit_block_gen o_block v_block 
      in if o_b == v_b
         then (InitDecl o_b no_block, o_edit, v_edit)
         else error "unsupported differences"
    _ -> error "unsupported differences"

edit_member_gen :: MemberDecl -> MemberDecl -> (MemberDecl,Edit,Edit)
edit_member_gen o_mem v_mem = 
  case (o_mem,v_mem) of
    (FieldDecl _ _ _,_) -> if o_mem == v_mem then (o_mem,[],[]) else error "unsupported differences"
    (MethodDecl o_mods o_tys o_ty o_id o_fpars o_ex o_mbody, MethodDecl v_mods v_tys v_ty v_id v_fpars v_ex v_mbody) ->
      let checks = [ o_mods == v_mods
                   , o_tys == v_tys
                   , o_ty == v_ty
                   , o_id == v_id
                   , o_fpars == v_fpars
                   , o_ex == v_ex ]
          (no_mbody,o_edit,v_edit) = edit_method_body_gen o_mbody v_mbody
      in if all id checks
         then (MethodDecl o_mods o_tys o_ty o_id o_fpars o_ex no_mbody, o_edit, v_edit)
         else error "unsupported differences"
    (ConstructorDecl o_mods o_tys o_id o_fpars o_ex o_cbody, ConstructorDecl v_mods v_tys v_id v_fpars v_ex v_cbody) ->
      let checks = [ o_mods == v_mods
                   , o_tys == v_tys
                   , o_id == v_id
                   , o_fpars == v_fpars
                   , o_ex == v_ex ]
          (no_cbody,o_edit,v_edit) = edit_constructor_body_gen o_cbody v_cbody
      in if all id checks
         then (ConstructorDecl o_mods o_tys o_id o_fpars o_ex no_cbody, o_edit, v_edit)
         else error "unsupported differences"
    (MemberClassDecl o_class, MemberClassDecl v_class) ->
      let (no_class,o_edit,v_edit) = edit_class_gen o_class v_class
      in (MemberClassDecl no_class, o_edit, v_edit) 
    _ -> error "unsupported differences"

edit_method_body_gen :: MethodBody -> MethodBody -> (MethodBody,Edit,Edit)
edit_method_body_gen o_mbody v_mbody =
  if o_mbody == v_mbody
  then (o_mbody, [], [])
  else -- the method bodies are for sure not the same
    case (o_mbody, v_mbody) of
      (MethodBody Nothing, MethodBody (Just block)) -> (MethodBody $ Just $ Block [hole], [skip], [BlockStmt $ StmtBlock block])
      (MethodBody (Just block), MethodBody Nothing) -> (MethodBody $ Just $ Block [hole], [BlockStmt $ StmtBlock block], [skip])
      (MethodBody (Just o_block), MethodBody (Just v_block)) ->
        let (no_block,o_edit,v_edit) = edit_block_gen o_block v_block
        in (MethodBody $ Just no_block, o_edit, v_edit) 

edit_constructor_body_gen :: ConstructorBody -> ConstructorBody -> (ConstructorBody,Edit,Edit)
edit_constructor_body_gen o_cbody v_cbody =
  if o_cbody == v_cbody
  then (o_cbody, [], [])
  else case (o_cbody, v_cbody) of
    (ConstructorBody o_e o_stmts, ConstructorBody v_e v_stmts) -> error "todo" 

edit_block_gen :: Block -> Block -> (Block,Edit,Edit)
edit_block_gen (Block o_block) (Block v_block) =
  let c = lcs o_block v_block
      (no_block,o_edit,v_edit) = diff2edit o_block v_block c (length o_block, length v_block) ([],[],[]) 
  in (Block no_block,o_edit,v_edit)
