(ns vocational-education.governor
  "VocationalEducationGovernor — the independent safety/traceability
  layer for the ISCO-08 2320 independent vocational-education actor.
  Wired as its own `:govern` node in `vocational-education.actor`'s
  StateGraph, downstream of `:advise` — the Advisor has no notion of
  student provenance or power-tool/heavy-equipment risk, so this MUST
  be a separate system able to reject a proposal (itonami actor
  pattern, per ADR-2607011000 / CLAUDE.md Actors section).

  `check` is a pure function of (request, context, proposal, store) ->
  verdict; it never mutates the store. The StateGraph's `:decide` node
  routes on the verdict:
    :hard? true                → :hold  (irreversible, no write)
    :escalate? true            → :request-approval (interrupt-before)
    otherwise                  → :commit

  HARD invariants (:hard? true, ALWAYS :hold, never overridable):
    1. student provenance  — the request's student must be registered.
    2. no-actuation          — proposal :effect must be :propose.
  ESCALATION invariants (:escalate? true, ALWAYS human sign-off, per the
  README robotics-premise: operating near students using power tools
  or heavy equipment always require human sign-off):
    3. :op :operate-power-tool-near-student.
    4. :op :operate-heavy-equipment-near-student.
    5. low confidence (< `confidence-floor`)."
  (:require [vocational-education.store :as store]))

(def confidence-floor 0.6)
(def ^:private escalating-ops #{:operate-power-tool-near-student :operate-heavy-equipment-near-student})

(defn- hard-violations [{:keys [proposal]} student-record]
  (cond-> []
    (nil? student-record)
    (conj {:rule :no-student :detail "未登録 student"})

    (not= :propose (:effect proposal))
    (conj {:rule :no-actuation :detail "effect は :propose のみ許可（直接書込禁止）"})))

(defn check
  "Assess a proposal against `request`/`context`/`proposal` and a
  `store` implementing `vocational-education.store/Store`. Returns
  `{:ok? bool :violations [...] :confidence n :hard? bool :escalate? bool}`."
  [request context proposal store]
  (let [student-record (store/student store (:student-id request))
        hard (hard-violations {:proposal proposal} student-record)
        hard? (boolean (seq hard))
        conf (or (:confidence proposal) 0.0)
        low? (< conf confidence-floor)
        risky-op? (contains? escalating-ops (:op proposal))]
    {:ok? (and (not hard?) (not low?) (not risky-op?))
     :violations hard
     :confidence conf
     :hard? hard?
     :escalate? (and (not hard?) (or low? risky-op?))}))
