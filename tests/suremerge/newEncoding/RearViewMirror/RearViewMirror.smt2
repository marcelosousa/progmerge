(set-logic HORN)
(declare-fun Q_n0 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_exit (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n1 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n1_1a (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n1_2a (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n2 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n2_3b (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n3 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n4 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n5 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n6 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int)) (=> (and (= xo xa) (= xa xb) (= xb xm) (= yo ya) (= ya yb) (= yb ym) (= zo za) (= za zb) (= zb zm)) (Q_n0 xo xa xb xm yo ya yb ym zo za zb zm))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int)) (=> (Q_n0 xo xa xb xm yo ya yb ym zo za zb zm) (Q_n1 xo xa xb xm yo ya yb ym zo za zb zm))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int)) (=> (Q_n0 xo xa xb xm yo ya yb ym zo za zb zm) (Q_n4 xo xa xb xm yo ya yb ym zo za zb zm))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int)) (=> (Q_n1 xo xa xb xm yo ya yb ym zo za zb zm) (Q_n1_1a xo xa xb xm yo ya yb ym zo za zb zm))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int)) (=> (and (= xm 1) (= xb 1) (= xa 1) (= xo 1) (Q_n1_1a xo xa xb xm yo ya yb ym zo za zb zm)) (Q_n1_2a xo xa xb xm yo ya yb ym zo za zb zm))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int)) (=> (and (= zm 3) (= za 3) (Q_n1_2a xo xa xb xm yo ya yb ym zo za zb zm)) (Q_n2 xo xa xb xm yo ya yb ym zo za zb zm))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int)) (=> (Q_n2 xo xa xb xm yo ya yb ym zo za zb zm) (Q_n2_3b xo xa xb xm yo ya yb ym zo za zb zm))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int) (yb1 Int) (ym1 Int)) (=> (and (= ym1 2) (= yb1 2) (Q_n2_3b xo xa xb xm yo ya yb ym zo za zb zm)) (Q_exit xo xa xb xm yo ya yb1 ym1 zo za zb zm))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int) (yo1 Int) (ya1 Int) (yb1 Int) (ym1 Int)) (=> (and (= ym1 2) (= yb1 2) (= ya1 2) (= yo1 2) (Q_n3 xo xa xb xm yo ya yb ym zo za zb zm)) (Q_n4 xo xa xb xm yo1 ya1 yb1 ym1 zo za zb zm))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int)) (=> (Q_n4 xo xa xb xm yo ya yb ym zo za zb zm) (Q_exit xo xa xb xm yo ya yb ym zo za zb zm))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int)) (=> (and (not (= xm 1)) (not (= xb 1)) (not (= xa 1)) (not (= xo 1)) (Q_n5 xo xa xb xm yo ya yb ym zo za zb zm)) (Q_n6 xo xa xb xm yo ya yb ym zo za zb zm))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int)) (=> (Q_n6 xo xa xb xm yo ya yb ym zo za zb zm) (Q_exit xo xa xb xm yo ya yb ym zo za zb zm))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int)) (=> (Q_exit xo xa xb xm yo ya yb ym zo za zb zm) (and (and (or (= xo xa) (= xm xa)) (or (= xo xb) (= xm xb)) (or (not (= xo xa)) (not (= xo xb)) (= xm xo))) (and (or (= yo ya) (= ym ya)) (or (= yo yb) (= ym yb)) (or (not (= yo ya)) (not (= yo yb)) (= ym yo))) (and (or (= zo za) (= zm za)) (or (= zo zb) (= zm zb)) (or (not (= zo za)) (not (= zo zb)) (= zm zo)))))))
(check-sat)