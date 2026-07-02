(ns vocational-education.store
  "SSoT for the ISCO-08 2320 independent vocational-education sole-
  proprietor actor. Store is a protocol injected into the
  `vocational-education.actor` StateGraph — `MemStore` is the default,
  deterministic, zero-dep backend; a Datomic/kotoba-server-backed
  implementation can be swapped in without touching the actor or
  governor (itonami actor pattern, per ADR-2607011000 / CLAUDE.md
  Actors section).

  Domain:

    student  — a registered enrolled student (:student-id, :name)
    record   — a committed operating record under a student (teach
               support, assessment, power-tool-near-student operation,
               heavy-equipment-near-student operation) — written ONLY
               via commit-record!, never mutated in place
    ledger   — an append-only audit trail of every proposal/verdict/
               disposition, regardless of outcome (commit or hold)")

(defprotocol Store
  (student [s student-id])
  (records-of [s student-id])
  (ledger [s])
  (register-student! [s student])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (student [_ student-id] (get-in @a [:students student-id]))
  (records-of [_ student-id] (filter #(= student-id (:student-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-student! [s student]
    (swap! a assoc-in [:students (:student-id student)] student) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:students {} :records [] :ledger []} seed)))))
