{-# LANGUAGE DeriveDataTypeable #-}
-------------------------------------------------------------------------------
-- Module    :  Edit 
-- Copyright :  (c) 2016 Marcelo Sousa
-------------------------------------------------------------------------------
module Edit where

import Language.Java.Parser hiding (opt)
import Language.Java.Pretty hiding (opt)
import Language.Java.Syntax

import Data.Map (Map)
import qualified Data.Map as M

import qualified Debug.Trace as T
import Edit.Diff
import Edit.Types
import Edit.Apply
import Edit.Gen
import Edit.Normalize

test_edit_gen :: FilePath -> IO Program 
test_edit_gen orig = do
  orig_ast <- parser compilationUnit `fmap` readFile orig
  case orig_ast of
    Right o_ast -> return o_ast 
    Left err -> error $ "parse error..." ++ show err

main_edit_gen :: FilePath -> FilePath -> IO () --CompilationUnit, Edit, Edit
main_edit_gen orig var = do
  orig_ast <- parser compilationUnit `fmap` readFile orig
  var_ast  <- parser compilationUnit `fmap` readFile var 
  case (orig_ast, var_ast) of
    (Right o_ast, Right v_ast) -> do
      let (no_ast, o_edit, v_edit) = edit_gen o_ast v_ast
      putStrLn $ prettyPrint no_ast
      print o_edit
      print v_edit  
    _ -> error "parse error..."

-- | Parses the 4 files
kast :: FilePath -> FilePath -> FilePath -> FilePath -> IO (Program,Program,Program,Program)
kast ofl afl bfl mfl = do
  _o <- parser compilationUnit `fmap` readFile ofl 
  _a <- parser compilationUnit `fmap` readFile afl 
  _b <- parser compilationUnit `fmap` readFile bfl 
  _m <- parser compilationUnit `fmap` readFile mfl 
  case (_o,_a,_b,_m) of
    (Right o, Right a, Right b, Right m) -> return (o,a,b,m)

-- Main function that gets the edit scripts
kedits :: FilePath -> FilePath -> FilePath -> FilePath -> IO ()
kedits ofl afl bfl mfl = do
  (o,a,b,m) <- kast ofl afl bfl mfl
  let (no, ea,eo) = edit_gen o a -- (no,ea) = gen_edit o a []
      (nno,eab)   = gen_edit no b [eo,ea]
      (nnno,eabm) = gen_edit nno m eab
      (fo,es@[e_a,e_b,e_m,e_o]) = gen_edit nnno o eabm
      pairs = [(o,fo,e_o),(a,fo,e_a),(b,fo,e_b),(m,fo,e_m)]
  putStrLn $ prettyPrint fo 
  mapM_ print es
  mapM_ (print . check_edit_soundness) pairs
 
gen_edit :: Program -> Program -> [Edit] -> (Program, [Edit])
gen_edit p1 p2 eis =
  let (p,e1,e2) = edit_gen p1 p2
      -- (p',e2,e1') = edit_gen p2 p1
      -- assertions: p == p', e1 == e1', e2 == e2', |e1| == |e2'|
      e = zip e1 e2
      -- need to update the others
      (_,eis') = foldl (gen_edit_aux eis) (0,[]) e
  in (p,eis' ++ [e1])
 where
  gen_edit_aux :: [Edit] -> (Int,[Edit]) -> (BlockStmt,BlockStmt) -> (Int,[Edit])
  gen_edit_aux eis (i,eis') (e1,e2)
   | e1 == skip || e2 == hole = (i+1,push (map (\e -> e!!i) eis) eis') 
   | otherwise = (i, push (replicate (length eis) e2) eis')

-- add each x to the ei
push :: [BlockStmt] -> [Edit] -> [Edit]
push xs [] = map (:[]) xs
push xs eis =
  let a = zip xs eis
  in map (\(x,ei) -> ei++[x]) a

-- | Check the soundness of the edit script 
check_edit_soundness :: (Program,Program,Edit) -> Bool
check_edit_soundness (original,holes,edit) = 
  original == (apply_edit holes edit)

