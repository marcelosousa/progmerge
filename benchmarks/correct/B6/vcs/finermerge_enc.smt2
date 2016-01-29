(set-logic HORN)
(declare-fun Q_n0 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n3 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n0_1 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n0_2 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n0_3 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n0_t (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n1 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n2 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n2_b (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n2_e (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n2_e1 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n2_t (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n2_t1 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (and (= wo wa) (= wa wb) (= wb wm) (= xo xa) (= xa xb) (= xb xm) (= yo ya) (= ya yb) (= yb ym)) (Q_n0 wo wa wb wm xo xa xb xm yo ya yb ym))))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (Q_n0 wo wa wb wm xo xa xb xm yo ya yb ym) true)))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (Q_n0 wo wa wb wm xo xa xb xm yo ya yb ym) (Q_n0_1 wo wa wb wm xo xa xb xm yo ya yb ym))))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (Q_n0_1 wo wa wb wm xo xa xb xm yo ya yb ym) true)))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (Q_n0_1 wo wa wb wm xo xa xb xm yo ya yb ym) (Q_n0_2 wo wa wb wm xo xa xb xm yo ya yb ym))))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (Q_n0_1 wo wa wb wm xo xa xb xm yo ya yb ym) (Q_n0_3 wo wa wb wm xo xa xb xm yo ya yb ym))))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (Q_n0_2 wo wa wb wm xo xa xb xm yo ya yb ym) (= (<= ya 0)))))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (and (<= ya 0) (Q_n0_2 wo wa wb wm xo xa xb xm yo ya yb ym)) (Q_n0_t wo wa wb wm xo xa xb xm yo ya yb ym))))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (Q_n0_3 wo wa wb wm xo xa xb xm yo ya yb ym) (= (> ya 0)))))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (and (> ya 0) (Q_n0_3 wo wa wb wm xo xa xb xm yo ya yb ym)) (Q_n1 wo wa wb wm xo xa xb xm yo ya yb ym))))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (wa1 Int)) (=> (Q_n0_t wo wa wb wm xo xa xb xm yo ya yb ym) true)))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (wa1 Int)) (=> (and (= wa1 (+ wa 1)) (Q_n0_t wo wa wb wm xo xa xb xm yo ya yb ym)) (Q_n1 wo wa1 wb wm xo xa xb xm yo ya yb ym))))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (xo1 Int) (xa1 Int) (xb1 Int) (xm1 Int)) (=> (Q_n1 wo wa wb wm xo xa xb xm yo ya yb ym) true)))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (xo1 Int) (xa1 Int) (xb1 Int) (xm1 Int)) (=> (and (= xm1 1) (= xb1 1) (= xa1 1) (= xo1 1) (Q_n1 wo wa wb wm xo xa xb xm yo ya yb ym)) (Q_n2 wo wa wb wm xo1 xa1 xb1 xm1 yo ya yb ym))))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (Q_n2 wo wa wb wm xo xa xb xm yo ya yb ym) true)))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (Q_n2 wo wa wb wm xo xa xb xm yo ya yb ym) (Q_n2_b wo wa wb wm xo xa xb xm yo ya yb ym))))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (Q_n2_b wo wa wb wm xo xa xb xm yo ya yb ym) true)))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (Q_n2_b wo wa wb wm xo xa xb xm yo ya yb ym) (Q_n2_t wo wa wb wm xo xa xb xm yo ya yb ym))))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (Q_n2_b wo wa wb wm xo xa xb xm yo ya yb ym) (Q_n2_e wo wa wb wm xo xa xb xm yo ya yb ym))))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (Q_n2_e wo wa wb wm xo xa xb xm yo ya yb ym) (= (> yb 0) (> ym 0)))))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (and (> ym 0) (> yb 0) (Q_n2_e wo wa wb wm xo xa xb xm yo ya yb ym)) (Q_n2_e1 wo wa wb wm xo xa xb xm yo ya yb ym))))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (wb1 Int) (wm1 Int)) (=> (Q_n2_e1 wo wa wb wm xo xa xb xm yo ya yb ym) true)))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (wb1 Int) (wm1 Int)) (=> (and (= wm1 (- wm 1)) (= wb1 (- wb 1)) (Q_n2_e1 wo wa wb wm xo xa xb xm yo ya yb ym)) (Q_n3 wo wa wb1 wm1 xo xa xb xm yo ya yb ym))))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (Q_n2_t wo wa wb wm xo xa xb xm yo ya yb ym) (= (<= yb 0) (<= ym 0)))))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (and (<= ym 0) (<= yb 0) (Q_n2_t wo wa wb wm xo xa xb xm yo ya yb ym)) (Q_n2_t1 wo wa wb wm xo xa xb xm yo ya yb ym))))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (wb1 Int) (wm1 Int)) (=> (Q_n2_t1 wo wa wb wm xo xa xb xm yo ya yb ym) true)))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int) (wb1 Int) (wm1 Int)) (=> (and (= wm1 (+ wm 1)) (= wb1 (+ wb 1)) (Q_n2_t1 wo wa wb wm xo xa xb xm yo ya yb ym)) (Q_n3 wo wa wb1 wm1 xo xa xb xm yo ya yb ym))))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (Q_n3 wo wa wb wm xo xa xb xm yo ya yb ym) (and (and (or (= wo wa) (= wm wa)) (or (= wo wb) (= wm wb)) (or (not (= wo wa)) (not (= wo wb)) (= wm wo))) (and (or (= xo xa) (= xm xa)) (or (= xo xb) (= xm xb)) (or (not (= xo xa)) (not (= xo xb)) (= xm xo))) (and (or (= yo ya) (= ym ya)) (or (= yo yb) (= ym yb)) (or (not (= yo ya)) (not (= yo yb)) (= ym yo)))))))
(assert (forall ((wo Int) (wa Int) (wb Int) (wm Int) (xo Int) (xa Int) (xb Int) (xm Int) (yo Int) (ya Int) (yb Int) (ym Int)) (=> (Q_n1 wo wa wb wm xo xa xb xm yo ya yb ym) (and (and (or (= wo wa) (= wm wa)) (or (= wo wb) (= wm wb)) (or (not (= wo wa)) (not (= wo wb)) (= wm wo))) (and (or (= xo xa) (= xm xa)) (or (= xo xb) (= xm xb)) (or (not (= xo xa)) (not (= xo xb)) (= xm xo))) (and (or (= yo ya) (= ym ya)) (or (= yo yb) (= ym yb)) (or (not (= yo ya)) (not (= yo yb)) (= ym yo)))))))
(check-sat)
