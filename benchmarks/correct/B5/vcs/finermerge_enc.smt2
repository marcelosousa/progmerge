(set-logic HORN)
(declare-fun Q_n0 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_exit (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n0_b (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n1 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n1_1 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n1_a (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n2 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n2_1 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n2_a (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n3 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n3_a (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(declare-fun Q_n4 (Int Int Int Int Int Int Int Int Int Int Int Int) Bool)
(assert (forall ((gradeo Int) (gradea Int) (gradeb Int) (gradem Int) (ro Int) (ra Int) (rb Int) (rm Int) (reso Int) (resa Int) (resb Int) (resm Int)) (=> (and (= gradeo gradea) (= gradea gradeb) (= gradeb gradem) (= ro ra) (= ra rb) (= rb rm) (= reso resa) (= resa resb) (= resb resm)) (Q_n0 gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm))))
(assert (forall ((gradeo Int) (gradea Int) (gradeb Int) (gradem Int) (ro Int) (ra Int) (rb Int) (rm Int) (reso Int) (resa Int) (resb Int) (resm Int)) (=> (Q_n0 gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm) (Q_n0_b gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm))))
(assert (forall ((gradeo Int) (gradea Int) (gradeb Int) (gradem Int) (ro Int) (ra Int) (rb Int) (rm Int) (reso Int) (resa Int) (resb Int) (resm Int)) (=> (Q_n0_b gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm) (Q_n1 gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm))))
(assert (forall ((gradeo Int) (gradea Int) (gradeb Int) (gradem Int) (ro Int) (ra Int) (rb Int) (rm Int) (reso Int) (resa Int) (resb Int) (resm Int)) (=> (Q_n0_b gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm) (Q_n2 gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm))))
(assert (forall ((gradeo Int) (gradea Int) (gradeb Int) (gradem Int) (ro Int) (ra Int) (rb Int) (rm Int) (reso Int) (resa Int) (resb Int) (resm Int)) (=> (Q_n0_b gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm) (Q_n4 gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm))))
(assert (forall ((gradeo Int) (gradea Int) (gradeb Int) (gradem Int) (ro Int) (ra Int) (rb Int) (rm Int) (reso Int) (resa Int) (resb Int) (resm Int)) (=> (and (= gradem 1) (= gradeb 1) (= gradea 1) (= gradeo 1) (Q_n1 gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm)) (Q_n1_1 gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm))))
(assert (forall ((gradeo Int) (gradea Int) (gradeb Int) (gradem Int) (ro Int) (ra Int) (rb Int) (rm Int) (reso Int) (resa Int) (resb Int) (resm Int)) (=> (Q_n1_1 gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm) (Q_n1_a gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm))))
(assert (forall ((gradeo Int) (gradea Int) (gradeb Int) (gradem Int) (ro Int) (ra Int) (rb Int) (rm Int) (reso Int) (resa Int) (resb Int) (resm Int) (ro1 Int) (rb1 Int) (resa1 Int) (resm1 Int)) (=> (and (= resm1 1) (= rb1 1) (= resa1 1) (= ro1 1) (Q_n1_a gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm)) (Q_n3 gradeo gradea gradeb gradem ro1 ra rb1 rm reso resa1 resb resm1))))
(assert (forall ((gradeo Int) (gradea Int) (gradeb Int) (gradem Int) (ro Int) (ra Int) (rb Int) (rm Int) (reso Int) (resa Int) (resb Int) (resm Int)) (=> (and (= gradem 2) (= gradeb 2) (= gradea 2) (= gradeo 2) (Q_n2 gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm)) (Q_n2_1 gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm))))
(assert (forall ((gradeo Int) (gradea Int) (gradeb Int) (gradem Int) (ro Int) (ra Int) (rb Int) (rm Int) (reso Int) (resa Int) (resb Int) (resm Int)) (=> (Q_n2_1 gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm) (Q_n2_a gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm))))
(assert (forall ((gradeo Int) (gradea Int) (gradeb Int) (gradem Int) (ro Int) (ra Int) (rb Int) (rm Int) (reso Int) (resa Int) (resb Int) (resm Int) (ro1 Int) (rb1 Int) (resa1 Int) (resm1 Int)) (=> (and (= resm1 2) (= rb1 2) (= resa1 2) (= ro1 2) (Q_n2_a gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm)) (Q_n3 gradeo gradea gradeb gradem ro1 ra rb1 rm reso resa1 resb resm1))))
(assert (forall ((gradeo Int) (gradea Int) (gradeb Int) (gradem Int) (ro Int) (ra Int) (rb Int) (rm Int) (reso Int) (resa Int) (resb Int) (resm Int)) (=> (Q_n3 gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm) (Q_n3_a gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm))))
(assert (forall ((gradeo Int) (gradea Int) (gradeb Int) (gradem Int) (ro Int) (ra Int) (rb Int) (rm Int) (reso Int) (resa Int) (resb Int) (resm Int) (reso1 Int) (resb1 Int) (resm1 Int)) (=> (and (= resm1 rm) (= resb1 rb) (= reso1 ro) (Q_n3_a gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm)) (Q_exit gradeo gradea gradeb gradem ro ra rb rm reso1 resa resb1 resm1))))
(assert (forall ((gradeo Int) (gradea Int) (gradeb Int) (gradem Int) (ro Int) (ra Int) (rb Int) (rm Int) (reso Int) (resa Int) (resb Int) (resm Int) (rb1 Int) (rm1 Int)) (=> (and (= rm1 0) (= rb1 0) (Q_n4 gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm)) (Q_n3 gradeo gradea gradeb gradem ro ra rb1 rm1 reso resa resb resm))))
(assert (forall ((gradeo Int) (gradea Int) (gradeb Int) (gradem Int) (ro Int) (ra Int) (rb Int) (rm Int) (reso Int) (resa Int) (resb Int) (resm Int)) (=> (Q_exit gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm) (and (and (or (= gradeo gradea) (= gradem gradea)) (or (= gradeo gradeb) (= gradem gradeb)) (or (not (= gradeo gradea)) (not (= gradeo gradeb)) (= gradem gradeo))) (and (or (= ro ra) (= rm ra)) (or (= ro rb) (= rm rb)) (or (not (= ro ra)) (not (= ro rb)) (= rm ro))) (and (or (= reso resa) (= resm resa)) (or (= reso resb) (= resm resb)) (or (not (= reso resa)) (not (= reso resb)) (= resm reso)))))))
(assert (forall ((gradeo Int) (gradea Int) (gradeb Int) (gradem Int) (ro Int) (ra Int) (rb Int) (rm Int) (reso Int) (resa Int) (resb Int) (resm Int)) (=> (Q_n3 gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm) (and (and (or (= gradeo gradea) (= gradem gradea)) (or (= gradeo gradeb) (= gradem gradeb)) (or (not (= gradeo gradea)) (not (= gradeo gradeb)) (= gradem gradeo))) (and (or (= ro ra) (= rm ra)) (or (= ro rb) (= rm rb)) (or (not (= ro ra)) (not (= ro rb)) (= rm ro))) (and (or (= reso resa) (= resm resa)) (or (= reso resb) (= resm resb)) (or (not (= reso resa)) (not (= reso resb)) (= resm reso)))))))
(assert (forall ((gradeo Int) (gradea Int) (gradeb Int) (gradem Int) (ro Int) (ra Int) (rb Int) (rm Int) (reso Int) (resa Int) (resb Int) (resm Int)) (=> (Q_n1_1 gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm) (and (and (or (= gradeo gradea) (= gradem gradea)) (or (= gradeo gradeb) (= gradem gradeb)) (or (not (= gradeo gradea)) (not (= gradeo gradeb)) (= gradem gradeo))) (and (or (= ro ra) (= rm ra)) (or (= ro rb) (= rm rb)) (or (not (= ro ra)) (not (= ro rb)) (= rm ro))) (and (or (= reso resa) (= resm resa)) (or (= reso resb) (= resm resb)) (or (not (= reso resa)) (not (= reso resb)) (= resm reso)))))))
(assert (forall ((gradeo Int) (gradea Int) (gradeb Int) (gradem Int) (ro Int) (ra Int) (rb Int) (rm Int) (reso Int) (resa Int) (resb Int) (resm Int)) (=> (Q_n2_1 gradeo gradea gradeb gradem ro ra rb rm reso resa resb resm) (and (and (or (= gradeo gradea) (= gradem gradea)) (or (= gradeo gradeb) (= gradem gradeb)) (or (not (= gradeo gradea)) (not (= gradeo gradeb)) (= gradem gradeo))) (and (or (= ro ra) (= rm ra)) (or (= ro rb) (= rm rb)) (or (not (= ro ra)) (not (= ro rb)) (= rm ro))) (and (or (= reso resa) (= resm resa)) (or (= reso resb) (= resm resb)) (or (not (= reso resa)) (not (= reso resb)) (= resm reso)))))))
(check-sat)
