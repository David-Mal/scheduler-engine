# Decisions

## 1. Pure Java engine, Spring added later
**Decision:** The scheduling engine is plain Java (no framework). Spring Boot is added later as a separate module that uses the engine as a library.
**Why:** The algorithms and constraints don't need a framework, and keeping them independent makes them easy to unit test and reuse. It also keeps the core clean of web and database concerns.
**Trade-off:** I have to write the mapping between engine objects and database entities myself later.

## 2. Each scheduling rule is a separate Constraint object

**Decision:** every rule from [`RULES.md`](RULES.md) is its own class implementing
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

## 3. `Schedule` is mutable inside the engine

**Decision:** `Schedule` stays a mutable class with `add`, `remove` and `copy()`.
Immutability lives one level up: a saved plan version (database, later) is a
snapshot that never changes.

**Why:** local search tries thousands of small moves and undoes most of them.
Changing one assignment in place and reverting it is cheaper and simpler than
building a new schedule for every move. Undo/redo for the user is done with
stored versions, not with in-memory state.

**Trade-off:** a strategy must call `copy()` before it keeps a "best so far"
schedule, otherwise later moves change it too.

## 4. The previous month is a separate, read-only input

**Decision:** assignments from the previous month are never added to `Schedule`.
They are passed in `ScheduleProblem` as their own type
(`PastAssignment(participantId, role, date)`), used only by rules that look back
(rotation in the first week, fairness over time).

**Why:**
- `MonthlyLimit` counts every assignment in the `Schedule`, so the schedule must
  contain exactly one month.
- Past assignments point to slots of another problem; `problem.slot(id)` would
  not find them. A date is all the look-back rules need.
- Solvers can never change the past by mistake.

**Trade-off:** rules that look back read from two sources (schedule and history).

## 5. Unknown references are a programming error

**Decision:** an assignment that points to a slot or participant missing from the
`ScheduleProblem` is not a `Violation`. It is a bug in the calling code, so it
fails fast with an exception. The check is done once, in `ScheduleValidator`,
before any constraint runs; constraints can assume all references are valid.

**Why:** a violation is something the user can fix by editing the plan. A broken
reference is not, and reporting it as one would hide the real problem.

**Trade-off:** a constraint called directly (without the validator) on a broken
schedule may throw or silently skip the bad assignment, depending on the rule.

## 6. One generic fairness rule instead of one per role

**Decision:** `FairSingingDistribution(weight)` became
`FairRoleDistribution(role, weight)`, created once per role.

**Why:** `RULES.md` asks for balance only for singing, but without the same rule
for prayer and sermon the score has nothing that spreads the load between
people, and the solvers would have very little to optimise.

**Trade-off:** all instances share the constraint name `FairRoleDistribution`;
the role is only in the message. Each role needs its own weight, to be tuned on
real data.

## 7. Empty slots are reported, not forbidden

**Decision:** rule 10 was reworded: the system tries hard to fill every slot,
but when it cannot, it returns the slot as uncovered with the reason instead of
failing. `SlotFullyCovered` stays a hard constraint, so a partial plan is
invalid until the user completes it.

**Why:** some months have no valid solution (too many people away). A partial
plan with clear reasons is more useful than an exception or a silently broken rule.
