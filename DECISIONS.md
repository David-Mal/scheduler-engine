# Decisions

## 2026-11-04: Pure Java engine, Spring added later
**Decision:** The scheduling engine is plain Java (no framework). Spring Boot is added later as a separate module that uses the engine as a library.
**Why:** The algorithms and constraints don't need a framework, and keeping them independent makes them easy to unit test and reuse. It also keeps the core clean of web and database concerns.
**Trade-off:** I have to write the mapping between engine objects and database entities myself later.