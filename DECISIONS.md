# Decisions

## 1. Pure Java engine, Spring added later
**Decision:** The scheduling engine is plain Java (no framework). Spring Boot is added later as a separate module that uses the engine as a library.
**Why:** The algorithms and constraints don't need a framework, and keeping them independent makes them easy to unit test and reuse. It also keeps the core clean of web and database concerns.
**Trade-off:** I have to write the mapping between engine objects and database entities myself later.

## 2. Each scheduling rule is a separate Constraint object

**Decision:** every rule from `rules.md` is its own class implementing
`Constraint` (`evaluate(Schedule, ScheduleProblem) -> List<Violation>`),
instead of one big validation method.

**Why:**
- Each rule can be tested in isolation, with a small schedule built for that rule only.
- Adding or removing a rule does not touch the others (open/closed principle).
- Hard rules (weight 0, invalidate the plan) and soft rules (weighted, form the score)
  use the same interface, so the solver treats them uniformly.
- Rules are stateless, so they can be reused by any strategy
  (greedy now, something smarter later) and by the validator.
- A `Violation` carries the rule name, slot and participant, so the UI can explain
  exactly what is wrong.

**Trade-off:** more classes, and some rules repeat similar grouping code.
Accepted for clarity; a shared helper can be extracted later if it starts to hurt.