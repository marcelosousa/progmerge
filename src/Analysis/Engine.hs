-------------------------------------------------------------------------------
-- Module    :  Analysis.Engine
-- Copyright :  (c) 2016 Marcelo Sousa
-------------------------------------------------------------------------------
module Analysis.Engine where

import Analysis.Types
import Analysis.Util

import Control.Monad.State.Strict
import Data.Map (Map)
import Data.Maybe

import Language.Java.Syntax
import Language.Java.Pretty

import System.IO.Unsafe
import Z3.Monad hiding (Params)

import qualified Data.Map as M
import qualified Debug.Trace as T

trace a b = b
--trace = T.trace

-- Performs a SAT-query.
checkSAT phi = do
  push 
  assert phi
  res <- check
  pop 1
  return res
  
helper pre post = do
  formula <- mkImplies pre post >>= \phi -> mkNot phi 
  assert formula
  (r, m) <- getModel
  trace ("helper: " ++ show r) $ return (r,m)

prelude :: [FormalParam] -> Z3 (Params, [AST])
prelude params = do
  let arity = 4
      ps = getParIdents params 
      parsId = map (\s -> map (\ar -> s ++ show ar) [1..arity]) ps 
  intSort <- mkIntSort
  pars <- mapM (mapM (\par -> mkFreshConst par intSort)) parsId
  let pars' = foldl (\r (k,v) -> M.insert (Ident k) v r) M.empty $ zip ps pars
  res <- mapM (\idx -> mkFreshConst ("res"++show idx) intSort) [1..arity]
  return (pars', res)

initial_SSAMap :: Params -> Res -> Z3 SSAMap
initial_SSAMap params res = do
  iSort <- mkIntSort
  fn <- mkFreshFuncDecl "null" [] iSort
  ast <- mkApp fn []
  let i = M.singleton (Ident "null") (replicate 4 (ast, iSort, 0))
      ps = M.map (\a -> [(e, iSort, 0) | e <- a]) params
      rs = M.fromList [(Ident "_res_", [(r, iSort, 0) | r <- res])] 
      i_ssa = M.union i (M.union rs ps)
  return i_ssa

initial_precond :: Params -> Z3 AST 
initial_precond params = do 
  let ps = snd $ unzip $ M.toList params
      ps' = map (\x -> [(e,e') | e <- x, e' <- x]) ps
  eqs <- mapM (\p -> mapM (\(e,e') -> mkEq e e') p) ps'
  pre <- mkAnd $ concat eqs 
  return pre

-- @TODO
postcond :: [AST] -> Z3 AST
postcond res = case res of
  [r_o, r_a, r_b, r_m] -> undefined
  _ -> error "postcond: invalid input" 

-- Encoding functions 
processParam :: FormalParam -> Z3 Sort
processParam (FormalParam mods ty _ _) = processType ty 

processType :: Type -> Z3 Sort
processType (PrimType ty) =
  case ty of
    BooleanT -> mkBoolSort
    _ -> mkIntSort -- error $ "processType: " ++ show ty ++ " not supported"
processType (RefType (ClassRefType (ClassType [(Ident name,[])]))) = mkIntSort
--  do
--    sym <- mkStringSymbol name
--    mkUninterpretedSort sym
processType ty@(RefType _) = error $ "processType: not supported " ++ show ty

processAssign :: AST -> AssignOp -> AST -> AST -> Z3 AST
processAssign lhs op rhs plhs =
  case op of 
    EqualA -> mkEq lhs rhs
    AddA -> do
      rhs' <- mkAdd [plhs, rhs]
      mkEq lhs rhs'
    _ -> error $ "processAssign: " ++ show op ++ " not supported"

enc_ident :: String -> Int -> Sort -> Z3 [AST]
enc_ident str i sort = mapM (\j -> do
  let nstr = str ++ "_" ++ show j ++ "_" ++ show i
  sym <- mkStringSymbol nstr
  mkVar sym sort) [1..4]
 
enc_new_var :: (SSAMap, AssignMap, AST) -> Sort -> VarDecl -> Int -> Z3 (SSAMap, AssignMap, AST)
enc_new_var (ssamap', _assmap, pre') sort (VarDecl varid mvarinit) i = do
  (ident, idAsts) <-
    case varid of
      VarId ident@(Ident str) -> do
        vars <- enc_ident str i sort
        return (ident, vars)
      _ -> error $ "enc_new_var: not supported " ++ show varid
  let nssamap = M.insert ident [(idAst, sort, i) | idAst <- idAsts] ssamap'
  case mvarinit of
    Nothing -> return (nssamap, _assmap, pre')
    Just (InitExp expr) -> do
      expAsts <- enc_exps nssamap expr
      let id_exp = zip idAsts expAsts
      eqIdExps <- mapM (\(idAst,expAst) -> mkEq idAst expAst) id_exp
      pre <- mkAnd (pre':eqIdExps)
      let assmap = M.insert ident expr _assmap
      return (nssamap, assmap, pre)
    Just _ -> error "enc_new_var: not supported"
  
enc_exps :: SSAMap -> Exp -> Z3 [AST]
enc_exps = undefined
{-
processExp env@(objSort, pars, res, fields, ssamap) expr =
  case expr of
    Lit lit -> processLit lit
    ExpName name -> processName env name []
    BinOp lhsE op rhsE -> do
      lhs <- processExp env lhsE
      rhs <- processExp env rhsE
      processBinOp op lhs rhs
    FieldAccess fldAccess -> error "processExp: FieldAccess not supported"
    PreMinus nexpr -> do 
      nexprEnc <- processExp env nexpr
      mkUnaryMinus nexprEnc
    MethodInv (MethodCall name args) -> do
      argsAST <- mapM (processExp env) args
      processName env name argsAST
    Cond cond _then _else -> do
      condEnc <- processExp env cond
      _thenEnc <- processExp env _then
      _elseEnc <- processExp env _else
      mkIte condEnc _thenEnc _elseEnc        
    PreNot nexpr -> do
      nexprEnc <- processExp env nexpr
      mkNot nexprEnc            
    _ -> error $  "processExpr: " ++ show expr

processLit :: Literal -> Z3 AST
processLit lit =
  case lit of
    Boolean True -> mkTrue
    Boolean False -> mkFalse
    Int i -> mkIntNum i
    Null -> mkIntNum 0 -- case M.lookup (Ident "null") ssamap of
--    Nothing -> error "processLit: null not found"
--    Just (ast, _, _) -> return ast
    _ -> error "processLit: not supported"

processName :: (Sort, Params, [AST], Fields, SSAMap) -> Name -> [AST] -> Z3 AST
processName env@(objSort, pars, res, fields, ssamap) (Name [obj]) [] =
  case M.lookup obj pars of
    Nothing -> case M.lookup obj ssamap of
      Nothing -> error $ "Can't find " ++ show obj
      Just (ast,_,_) -> return ast
    Just ast -> error "processName: TODO" -- return ast
processName env@(objSort, pars, res, fields, ssamap) (Name [ident]) args = do
  let fn = safeLookup ("processName: declared func")  ident fields
  mkApp fn args
processName env@(objSort, pars, res, fields, ssamap) (Name [Ident "Character",fnName]) args = do
  let fn = safeLookup ("processName: Field" ++ show fnName)  fnName fields
  mkApp fn args
processName env@(objSort, pars, res, fields, ssamap) (Name [Ident "Double",Ident "compare"]) args = do
  let fnName = Ident "compareDouble"
      fn = safeLookup ("processName: Field" ++ show fnName)  fnName fields
  mkApp fn args
processName env@(objSort, pars, res, fields, ssamap) (Name [Ident "Int",Ident "compare"]) args = do
  let fnName = Ident "compareInt"
      fn = safeLookup ("processName: Field" ++ show fnName)  fnName fields
  mkApp fn args
processName env@(objSort, pars, res, fields, ssamap) (Name [Ident "String",Ident "compareIgnoreCase"]) args = do
  let fnName = Ident "compareIgnoreCaseString"
      fn = safeLookup ("processName: Field" ++ show fnName)  fnName fields
  mkApp fn args
processName env@(objSort, pars, res, fields, ssamap) (Name [obj,field]) args = do
  let par = safeLookup ("processName: Object" ++ show obj) obj pars
      fn = safeLookup ("processName: Field" ++ show field)  field fields
  error "processName: TODO"
  -- mkApp fn (par:args)
processName env name args = error $  "processName: corner case" ++ show name

processBinOp :: Op -> AST -> AST -> Z3 AST
processBinOp op lhs rhs = do 
  case op of
    NotEq -> mkEq lhs rhs >>= \eq -> mkNot eq
    And -> mkAnd [lhs,rhs]
    Add -> mkAdd [lhs,rhs]
    Mult -> mkMul [lhs,rhs]
    Sub -> mkSub [lhs,rhs]
    LThan -> mkLt lhs rhs
    LThanE -> mkLe lhs rhs
    GThan -> mkGt lhs rhs
    GThanE -> mkGe lhs rhs
    Equal -> mkEq lhs rhs
    COr -> mkOr [lhs, rhs]
    CAnd -> mkAnd [lhs, rhs]
    _ -> error $ "processBinOp: not supported " ++ show op
-} 

-- SMT Utility Functions
mkAttribute :: Sort -> Fields -> MemberDecl -> Z3 Fields
mkAttribute objSort m mDecl =
  case mDecl of
    FieldDecl  mods ty vardecls -> do
      retSort <- processType ty
      foldM (\nm vardecl -> mkField nm vardecl objSort retSort) m vardecls 
    MethodDecl mods ty (Just rty) (Ident name) pars exTy (MethodBody Nothing) -> do
      retSort <- processType rty
      i <- mkIntSort
      parsSort <- mapM processParam pars
      fn <- mkFreshFuncDecl name (objSort:parsSort) retSort
      return $ M.insert (Ident name) fn m

mkObjectSort :: String -> Z3 Sort
mkObjectSort str = do
  myint <- mkStringSymbol str
  mkUninterpretedSort myint

mkField :: Fields -> VarDecl -> Sort -> Sort -> Z3 Fields
mkField m (VarDecl (VarId (Ident name)) Nothing) parSort retSort = do
  fn <- mkFreshFuncDecl name [parSort] retSort
  return $ M.insert (Ident name) fn m

replaceVariable :: String -> FuncDecl -> AST -> Z3 AST
replaceVariable a fnB ast = do
  kind <- getAstKind ast
  case kind of
    Z3_NUMERAL_AST    -> return ast
    Z3_APP_AST        -> do
      app <- toApp ast
      fn <- getAppDecl app
      sym <- getDeclName fn >>= getSymbolString
      if sym == a
      then do
        nParams <- getAppNumArgs app
        args <- mapM (\i -> getAppArg app i) [0..(nParams-1)]
        args' <- mapM (replaceVariable a fnB) args
        mkApp fnB args' --T.trace ("FN " ++ symName) $ mkApp fn args'
      else do 
        nParams <- getAppNumArgs app
        args <- mapM (\i -> getAppArg app i) [0..(nParams-1)]
        args' <- mapM (replaceVariable a fnB) args
        mkApp fn args' --T.trace ("FN " ++ symName) $ mkApp fn args'
    Z3_VAR_AST        -> return ast
    Z3_QUANTIFIER_AST -> return ast --error "traverse"
    Z3_SORT_AST       -> return ast
    Z3_FUNC_DECL_AST  -> return ast
    Z3_UNKNOWN_AST    -> return ast

