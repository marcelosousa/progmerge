module Examples.SimpleEncoding where

import Data.Map
import Types

-- simple straight line example for testing the encoding
-- base program:
-- n0: i = 1
-- n1: j = 2
-- exit:
p :: Program
p =
  let n0 = ("n0",[(Assign "i" $ C 1, "n1")])
      n1 = ("n1",[(Assign "j" $ C 2, "exit")])
      prog = fromList [n0,n1]
  in ("n0",prog,"exit")

-- edit for variant A:
-- n0: x = 0
a :: Edit
a =
  let n0_a = ("n0_a",[(Assign "x" $ C 0, "n0_a_exit")])
      eprog = ("n0_a",fromList [n0_a],"n0_a_exit")
  in fromList [("n0",eprog)]

-- edit for variant B:
-- n1: y = 1
b :: Edit
b =
  let n1_b = ("n1_b",[(Assign "y" $ C 1, "n1_b_exit")])
      eprog = ("n1_b",fromList [n1_b],"n1_b_exit")
  in fromList [("n1",eprog)]

-- merge candidant
-- n0 :-> n0_a - x = 0
-- n1 :-> n1_b - y = 1
m :: Edit
m = a `union` b