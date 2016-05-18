# Akka Streams

#### Auto-Fusing and Async Boundaries

- With the inital streams release every stage ran in a separate actor
  - High messaging overhead, caused performance issues
- Addressed with auto-fusing
  - By default, all stages run in single actor
- Can specify via `.async` that a stage should run in a separate actor from the rest
- Reach for this when running a stage/set of stages in parallel with the rest of the stream improves performance

```scala
val count = 5

Source(1 to count)
    .via(Flow[Int].map("abc " * _).async)
    .runWith(Sink.foreach(logToPage))
```

- In this case, one actor runs the `Source` + `Map`, forming an async boundary
- The rest of the flow, in this case just the `Sink`, runs in another actor
- Auto fusing can be disabled by setting `akka.stream.materializer.auto-fusing=off`
