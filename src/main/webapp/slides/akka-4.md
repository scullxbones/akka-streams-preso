# Akka Streams

#### Custom stages

- Can be useful for statefulness
  - But first: check out `Source.unfold` and `Source.unfoldAsync`
- Fairly low-level, should be a last resort

```scala
val content = "abcdefghijklmnopqrstuvwxyz0123456789"

Source(splitPairs(content))
    .via(Flow[String].map(ByteString.apply))
    .viaMat(Flow.fromGraph(new MurmurHasher()))(Keep.right)
    .toMat(Sink.foreach(fn))(Keep.left)
    .run()
```

<iframe class="sample" data-src="/samples/graph-stage"></iframe>
