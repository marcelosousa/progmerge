(set-logic HORN)
(declare-fun Q_n0 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_exit (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n1 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n1_1a (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n2 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n2_1b (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int)) (=> (and (= xo xa) (= xa xb) (= xb xm) (= yo ya) (= ya yb) (= yb ym) (= zo za) (= za zb) (= zb zm)) (Q_n0 xo xa xb xm yo ya yb ym zo za zb zm))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int) (xo1 Int) (xa1 Int) (xb1 Int) (xm1 Int)) (=> (Q_n0 xo xa xb xm yo ya yb ym zo za zb zm) true)))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int) (xo1 Int) (xa1 Int) (xb1 Int) (xm1 Int)) (=> (and (= xm1 42) (= xb1 42) (= xa1 42) (= xo1 42) (Q_n0 xo xa xb xm yo ya yb ym zo za zb zm)) (Q_n1 xo1 xa1 xb1 xm1 yo ya yb ym zo za zb zm))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int)) (=> (Q_n1 xo xa xb xm yo ya yb ym zo za zb zm) true)))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int)) (=> (Q_n1 xo xa xb xm yo ya yb ym zo za zb zm) (Q_n1_1a xo xa xb xm yo ya yb ym zo za zb zm))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int) (ya1 Int) (ym1 Int)) (=> (Q_n1_1a xo xa xb xm yo ya yb ym zo za zb zm) true)))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int) (ya1 Int) (ym1 Int)) (=> (and (= ym1 42) (= ya1 42) (Q_n1_1a xo xa xb xm yo ya yb ym zo za zb zm)) (Q_n2 xo xa xb xm yo ya1 yb ym1 zo za zb zm))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int)) (=> (Q_n2 xo xa xb xm yo ya yb ym zo za zb zm) true)))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int)) (=> (Q_n2 xo xa xb xm yo ya yb ym zo za zb zm) (Q_n2_1b xo xa xb xm yo ya yb ym zo za zb zm))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int) (zb1 Int) (zm1 Int)) (=> (Q_n2_1b xo xa xb xm yo ya yb ym zo za zb zm) true)))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int) (zb1 Int) (zm1 Int)) (=> (and (= zm1 42) (= zb1 42) (Q_n2_1b xo xa xb xm yo ya yb ym zo za zb zm)) (Q_exit xo xa xb xm yo ya yb ym zo za zb1 zm1))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int)) (=> (Q_exit xo xa xb xm yo ya yb ym zo za zb zm) (and (and (or (= xo xa) (= xm xa)) (or (= xo xb) (= xm xb)) (or (not (= xo xa)) (not (= xo xb)) (= xm xo))) (and (or (= yo ya) (= ym ya)) (or (= yo yb) (= ym yb)) (or (not (= yo ya)) (not (= yo yb)) (= ym yo))) (and (or (= zo za) (= zm za)) (or (= zo zb) (= zm zb)) (or (not (= zo za)) (not (= zo zb)) (= zm zo)))))))
(assert (forall ((xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (zo Int) (za Int) (zb Int) (zm Int)) (=> (Q_n2 xo xa xb xm yo ya yb ym zo za zb zm) (and (and (or (= xo xa) (= xm xa)) (or (= xo xb) (= xm xb)) (or (not (= xo xa)) (not (= xo xb)) (= xm xo))) (and (or (= yo ya) (= ym ya)) (or (= yo yb) (= ym yb)) (or (not (= yo ya)) (not (= yo yb)) (= ym yo))) (and (or (= zo za) (= zm za)) (or (= zo zb) (= zm zb)) (or (not (= zo za)) (not (= zo zb)) (= zm zo)))))))
(check-sat)
