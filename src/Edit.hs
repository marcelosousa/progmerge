{-# LANGUAGE DeriveDataTypeable #-}
{-# LANGUAGE RecordWildCards #-}
-------------------------------------------------------------------------------
-- Module    :  Edit 
-- Copyright :  (c) 2016 Marcelo Sousa
-------------------------------------------------------------------------------
module Edit where

import Analysis.Java.Liff
import Analysis.Java.ClassInfo
import Data.Map (Map)
import Edit.Apply
import Edit.Diff
import Edit.Gen
import Edit.Normalize
import Edit.Print
import Edit.Types
import Language.Java.Parser hiding (opt)
import Language.Java.Pretty hiding (opt)
import Language.Java.Syntax
import qualified Data.Map as M
import qualified Debug.Trace as T

type MethInst = (MIdent, MemberDecl, Edit, Edit, Edit, Edit)
type DiffInst = PMergeInst MethInst 

diffMethods :: MergeInst -> DiffInst
diffMethods m@MInst{..} = 
  let merges = foldr (diffMethods' m) []  _merges  
  in m { _merges = merges }
 where
  diffMethods' :: MergeInst -> MIdent -> [MethInst] -> [MethInst] 
  diffMethods' m@MInst{..} i@(cls,mth,tys) res =
    case (M.lookup cls _o_info,M.lookup cls _a_info,M.lookup cls _b_info,M.lookup cls _m_info) of
      (Just o, Just a, Just b, Just m) ->
        let o_ms = _cl_meths o
            a_ms = _cl_meths a
            b_ms = _cl_meths b
            m_ms = _cl_meths m
            m_s = (mth,tys)
        in case (M.lookup m_s o_ms,M.lookup m_s a_ms,M.lookup m_s b_ms,M.lookup m_s m_ms) of
          (Just m_o, Just m_a, Just m_b, Just m_m) ->
            let minst = diff4gen_meth i m_o m_a m_b m_m 
            in (minst:res)
          _ -> error $ "diffMethods: can't find info for method " ++ show m_s 
      _ -> error $ "diffMethods: can't find info for class " ++ show cls

diff4gen_meth :: MIdent -> MemberDecl -> MemberDecl -> MemberDecl -> MemberDecl -> MethInst
diff4gen_meth ident o a b m =
  let (no, eo, ea) = edit_member_gen o a 
      (nno, eab)   = gen_edit_member no b [eo,ea]
  in case gen_edit_member nno m eab of
      (fo,[[]]) -> (ident, fo, [], [], [], [])
      (fo, [e_o,e_a,e_b,e_m]) -> (ident, fo, e_o, e_a, e_b, e_m)
      (fo, es ) -> error $ show es

gen_edit_member :: MemberDecl -> MemberDecl -> [Edit] -> (MemberDecl, [Edit])
gen_edit_member p1 p2 eis =
  let (p,e1,e2) = edit_member_gen p1 p2
      -- (p',e2,e1') = edit_gen p2 p1
      -- assertions: p == p', e1 == e1', e2 == e2', |e1| == |e2'|
      e = zip e1 e2
      -- need to update the others
      (_,eis') = foldl (gen_edit_aux eis) (0,[]) e
  in (p,eis' ++ [e2])
 
diff4gen :: Program -> Program -> Program -> Program -> (Program, Edit, Edit, Edit, Edit)
diff4gen o a b m = 
  let (no, eo, ea) = edit_gen o a 
      (nno, eab)   = gen_edit no b [eo,ea]
  in case gen_edit nno m eab of
      (fo,[[]]) -> (fo, [], [], [], [])
      (fo, [e_o,e_a,e_b,e_m]) -> (fo, e_o, e_a, e_b, e_m)
      (fo, es ) -> error $ show es

gen_edit :: Program -> Program -> [Edit] -> (Program, [Edit])
gen_edit p1 p2 eis =
  let (p,e1,e2) = edit_gen p1 p2
      -- (p',e2,e1') = edit_gen p2 p1
      -- assertions: p == p', e1 == e1', e2 == e2', |e1| == |e2'|
      e = zip e1 e2
      -- need to update the others
      (_,eis') = foldl (gen_edit_aux eis) (0,[]) e
  in (p,eis' ++ [e2])

gen_edit_aux :: [Edit] -> (Int,[Edit]) -> (BlockStmt,BlockStmt) -> (Int,[Edit])
gen_edit_aux eis (i,eis') (e1,e2)
 | e2 == skip || e1 == hole = (i+1,push (map (\e -> e!!i) eis) eis') 
 | otherwise = (i, push (replicate (length eis) e1) eis')

-- add each x to the ei
push :: [BlockStmt] -> [Edit] -> [Edit]
push xs [] = map (:[]) xs
push xs eis =
  let a = zip xs eis
  in map (\(x,ei) -> ei++[x]) a

-- | Check the soundness of the edit script 
check_edit_soundness :: (Program, Program, Edit) -> Bool
check_edit_soundness (original, holes, edit) = 
  original == (apply_edit holes edit)
