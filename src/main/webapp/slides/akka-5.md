# Akka Streams

#### Auto-Fusing and Async Boundaries

- With the inital streams release every stage ran in a separate actor
  - High messaging overhead, caused performance issues
- Addressed with auto-fusing
  - By default, all stages run in single actor
  - Auto fusing can be disabled in configuration
- Can specify via `.async` that the flow so far should run in a separate actor from the rest
- Reach for this when running a stage/set of stages in parallel with the rest of the stream improves performance

```scala
val count = 5

Source(1 to count)
    .via(Flow[Int].map("abc " * _).async)
    .runWith(Sink.foreach(logToPage))
```
