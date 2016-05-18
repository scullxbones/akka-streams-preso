# Akka Streams

#### GraphDSL

- DSL to join together sources, flows, and sinks
- Allows composition of more complicated flow shapes
- Reach for this when you need fan-in and fan-out shapes
  - But first: check out `Source.combine` and `Sink.combine`

<a href="/resources/graphdslflow.png" target="blank">diagram</a>

```scala
def sample[Mat1,Mat2](source: Source[String, Mat1], tap: Sink[Int, Mat2]): Flow[String, String, Mat2] = {
    Flow.fromGraph(GraphDSL.create(source, tap)(Keep.right) { implicit builder => (src,snk) =>
        import GraphDSL.Implicits._

        val bcast = builder.add(Broadcast[String](2))
        val len = builder.add(Flow[String].map(_.length))
        val merge = builder.add(Merge[String](2))

        bcast.out(0)    ~> len          ~> snk.in
        bcast.out(1)    ~> merge.in(0)
        src.out         ~> merge.in(1)

        FlowShape(bcast.in, merge.out)
    })
}

val contained = "!!!" :: "@@@" :: Nil
val feed = "ABC" :: "abc" :: "de" :: Nil
val lengthPromise = Promise[Int]()
val lengthCalc = Sink.fold[Int,Int](0)(_ + _)

Source(feed)
    .viaMat(sample(Source(contained), lengthCalc))(Keep.right)
    .mapMaterializedValue(lengthPromise.completeWith)
    .toMat(Sink.foreach(logToPage))(Keep.right)
    .run()
```

<iframe class="sample" data-src="/samples/graph-dsl"></iframe>
