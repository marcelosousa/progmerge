(set-logic HORN)
(declare-fun even (Int) Int)
(declare-fun Q_n0 (Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n2 (Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n1 (Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n1_a (Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n1_b (Int Int Int Int Int Int Int Int) Bool)
;(set-option :fixedpoint.engine duality)
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (and (= xo xa) (= xa xb) (= xb xm) (= yo ya) (= ya yb) (= yb ym)) (Q_n0 xo xa xb xm yo ya yb ym))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (yo1 Int) (ya1 Int) (yb1 Int) (ym1 Int)) (=> (and (= ym1 0) (= yb1 0) (= ya1 0) (= yo1 0) (Q_n0 xo xa xb xm yo ya yb ym)) (Q_n1 xo xa xb xm yo1 ya1 yb1 ym1))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (Q_n1 xo xa xb xm yo ya yb ym) (Q_n1_a xo xa xb xm yo ya yb ym))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (xa1 Int) (xm1 Int)) (=> (and (= xm1 (even ym)) (= xa1 (even ya)) (Q_n1_a xo xa xb xm yo ya yb ym)) (Q_n1_b xo xa1 xb xm1 yo ya yb ym))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (xb1 Int) (xm1 Int)) (=> (and (= xm1 1) (= xb1 1) (Q_n1_b xo xa xb xm yo ya yb ym)) (Q_n2 xo xa xb1 xm1 yo ya yb ym))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (Q_n2 xo xa xb xm yo ya yb ym) (and (and (or (= xo xa) (= xm xa)) (or (= xo xb) (= xm xb)) (or (not (= xo xa)) (not (= xo xb)) (= xm xo))) (and (or (= yo ya) (= ym ya)) (or (= yo yb) (= ym yb)) (or (not (= yo ya)) (not (= yo yb)) (= ym yo)))))))
(check-sat)
