# Akka Streams

#### GraphDSL

- DSL to join together sources, flows, and sinks
- Allows composition of more complicated flow shapes
- Reach for this when you need fan-in and fan-out shapes
  - But first: check out `Source.combine` and `Sink.combine`

<a href="/resources/graphdslflow.png" target="blank">diagram</a>

```scala
def sample(source: Source[String, NotUsed]): Flow[String, String, NotUsed] = {
    Flow.fromGraph(GraphDSL.create(source) { implicit builder => src =>
        import GraphDSL.Implicits._

        val zipWith = builder.add(UnzipWith[String, String, String](s => s.take(2) -> s.drop(2)))
        val merge = builder.add(MergePreferred[String](2))

        zipWith.out0    ~> merge.in(0)
        zipWith.out1    ~> merge.in(1)
        src.out         ~> merge.preferred

        FlowShape(zipWith.in, merge.out)
    })
}

val contained = "!!!" :: "@@@" :: Nil
val feed = "ABC" :: "abc" :: "de" :: Nil

Source(feed)
    .via(sample(Source(contained)))
    .runWith(Sink.foreach(logToPage))
```

<iframe class="sample" data-src="/samples/graph-dsl"></iframe>
