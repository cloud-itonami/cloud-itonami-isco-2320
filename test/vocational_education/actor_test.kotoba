(ns vocational-education.actor-test
  (:require [clojure.test :refer [deftest is testing]]
            [vocational-education.actor :as actor]
            [vocational-education.store :as store]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-student! st {:student-id "student-1" :name "Alex Chen"})
    st))

(deftest commits-a-clean-low-risk-request
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:student-id "student-1" :op :assess :stake :low}
        result (actor/run-request! graph request {} "thread-1")]
    (is (= :done (:status result)))
    (is (some? (get-in result [:state :record])))
    (is (= 1 (count (store/records-of st "student-1"))))))

(deftest holds-on-unregistered-student-without-committing
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:student-id "no-such-student" :op :assess :stake :low}
        result (actor/run-request! graph request {} "thread-2")]
    (is (= :done (:status result)))
    (is (nil? (get-in result [:state :record])))
    (is (empty? (store/records-of st "no-such-student")))
    (is (= :hold (:disposition (:state result))))))

(deftest interrupts-then-commits-on-human-approval
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        ;; power-tool-near-student operation always escalates (governor invariant)
        request {:student-id "student-1" :op :operate-power-tool-near-student :stake :high}
        interrupted (actor/run-request! graph request {} "thread-3")]
    (is (= :interrupted (:status interrupted)))
    (is (empty? (store/records-of st "student-1")))
    (let [resumed (actor/approve! graph "thread-3")]
      (is (= :done (:status resumed)))
      (is (some? (get-in resumed [:state :record])))
      (is (= 1 (count (store/records-of st "student-1")))))))
