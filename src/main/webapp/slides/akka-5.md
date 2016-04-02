# Akka Streams

#### Auto-Fusing and Async Boundaries

- With the inital streams release every stage ran in a separate actor
  - High messaging overhead, caused performance issues
- Addressed with auto-fusing
- By default, all stages run in single actor
- Can specify via `.async` that a stage should run in a separate actor from the rest

```scala



```
