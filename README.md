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

## License

AGPL-3.0-or-later.
