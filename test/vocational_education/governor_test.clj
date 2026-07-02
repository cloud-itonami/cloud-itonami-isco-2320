(ns vocational-education.governor-test
  (:require [clojure.test :refer [deftest is testing]]
            [vocational-education.store :as store]
            [vocational-education.governor :as governor]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-student! st {:student-id "student-1" :name "Alex Chen"})
    st))

(deftest ok-on-clean-assess
  (let [st (fresh-store)
        proposal {:op :assess :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:student-id "student-1"} {} proposal st)]
    (is (:ok? v))
    (is (not (:hard? v)))
    (is (not (:escalate? v)))))

(deftest hard-on-unregistered-student
  (let [st (fresh-store)
        proposal {:op :assess :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:student-id "no-such-student"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-student (:rule %)) (:violations v)))))

(deftest hard-on-no-actuation-violation
  (let [st (fresh-store)
        proposal {:op :assess :effect :direct-write :confidence 0.9 :stake :low}
        v (governor/check {:student-id "student-1"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-actuation (:rule %)) (:violations v)))))

(deftest escalates-on-power-tool-near-student
  (let [st (fresh-store)
        proposal {:op :operate-power-tool-near-student :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:student-id "student-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest escalates-on-heavy-equipment-near-student
  (let [st (fresh-store)
        proposal {:op :operate-heavy-equipment-near-student :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:student-id "student-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest escalates-on-low-confidence
  (let [st (fresh-store)
        proposal {:op :assess :effect :propose :confidence 0.2 :stake :low}
        v (governor/check {:student-id "student-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest store-records-and-ledger-append-only
  (let [st (fresh-store)]
    (store/commit-record! st {:student-id "student-1" :op :teach-support})
    (store/append-ledger! st {:disposition :commit})
    (is (= 1 (count (store/records-of st "student-1"))))
    (is (= 1 (count (store/ledger st))))))
