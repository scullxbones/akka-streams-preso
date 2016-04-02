# Akka Streams

##### Map Async Supervision

- Supervision can cope with failure
- Simple case, `Supervision.resumingDecider` resumes the stream after the failure
- Finer detail can be done with a custom `Supervision` approach
- Completing the stream (rather than failing) can be done with `recover`

```scala
def asyncApi(str: String): Future[String] = Future {
    if (str == "X") throw new RuntimeException with NoStackTrace
    else if (str.nonEmpty && str.head.isLower)
        Thread.sleep(4 * Random.nextInt(250).toLong + 1000L)
    else Thread.sleep(Random.nextInt(100).toLong + 100L)
    str
}


val ids = "a" :: "B" :: "C" :: "D" :: "e" :: "F" :: "g" :: "H" :: "I" :: Nil
val parallelism = 2

Source(ids.take(parallelism) ++ ("X" :: ids.drop(parallelism)))
    .mapAsync(parallelism)(asyncApi _)
    .withAttributes(ActorAttributes.supervisionStrategy(Supervision.resumingDecider))
    .runWith(Sink.foreach(logToPage))
```

<iframe class="sample" data-src="/samples/map-async-supervision"></iframe>
