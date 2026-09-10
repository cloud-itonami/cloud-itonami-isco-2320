# cloud-itonami-isco-2320

Open Occupation Blueprint for **ISCO-08 2320**: Vocational Education Teachers.

This repository designs a forkable OSS business for an independent vocational education teacher: a workshop-support robot performs equipment setup and safety-checklist walkthroughs under a governor-gated actor, so the practice keeps its own enrollment and assessment records instead of renting a closed learning-management SaaS.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a workshop-support robot performs equipment setup, material handling and safety-checklist walkthroughs for hands-on training under an actor that proposes
actions and an independent **Vocational Education Governor** that gates them. The governor never
dispatches hardware itself; `:high`/`:safety-critical` actions (such as
operating near students using power tools or heavy equipment) require human sign-off.

A live sample of the operator console (robotics safety console, shared template) is rendered in [docs/samples/operator-console.html](docs/samples/operator-console.html) — pure-data HTML output of `kotoba.robotics.ui`.

## Core Contract

```text
enrollment + curriculum plan + safety checklist
        |
        v
Education Advisor -> Vocational Education Governor -> teach-support/assess, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, suppress
an operating record, or disclose sensitive data without governor approval and
audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `2320`). Required capabilities:

- :robotics
- :identity
- :forms
- :dmn
- :bpmn
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## Reference implementation (`:maturity :implemented`)

Full itonami Actor pattern (per ADR-2607011000 / CLAUDE.md's Actors
section, alongside `cloud-itonami-isco-6130`, `-8160`, `-2166`, `-2641`,
`-2651`, `-2652`, `-2654`, `-1219`, `-1223`, `-1330`, `-1341`, `-1349`,
`-1412`, `-1439` and `-2144`): a real
[`kotoba-lang/langgraph`](https://github.com/kotoba-lang/langgraph)
`StateGraph`, with the Advisor and Governor as distinct graph nodes and
human-in-the-loop interrupt/resume via checkpointing.

```text
:intake -> :advise -> :govern -> :decide -+-> :commit            (:ok? true)
                                           +-> :request-approval   (:escalate? true, interrupt-before)
                                           +-> :hold               (:hard? true)
```

- `src/vocational_education/store.kotoba` — `Store` protocol +
  `MemStore`: registered students, committed records, an append-only
  audit ledger.
- `src/vocational_education/advisor.kotoba` — `Advisor` protocol;
  `mock-advisor` (deterministic, default) proposes a training operation
  from a request; `llm-advisor` wraps a `langchain.model/ChatModel` —
  either way the advisor only ever produces a `:propose`-effect
  proposal, never a committed record, and LLM parse failures always
  yield `confidence 0.0` (forces escalation, never fabricated
  confidence).
- `src/vocational_education/governor.kotoba` —
  `VocationalEducationGovernor/check`: a pure function, wired as its
  own `:govern` node. Hard invariants (unregistered student, a
  proposal whose `:effect` isn't `:propose`) always route to `:hold`.
  Escalation invariants (`:operate-power-tool-near-student`,
  `:operate-heavy-equipment-near-student`, or low advisor confidence)
  always route to `:request-approval` — an `interrupt-before` node
  that the graph checkpoints and only resumes on explicit human
  approval (`actor/approve!`), matching the README's robotics-premise
  statement that operating near students using power tools or heavy
  equipment always require human sign-off.
- `src/vocational_education/actor.kotoba` — `build-graph`,
  `run-request!`, `approve!`: the `langgraph.graph/state-graph` wiring
  itself.

```bash
clojure -M:test
```

This is what backs this repo's `:maturity :implemented` entry in
[`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation).

## License

AGPL-3.0-or-later.
