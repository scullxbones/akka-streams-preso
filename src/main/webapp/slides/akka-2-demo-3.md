# Akka Streams

##### Map Async Failure

![mapasyncflow](/resources/mapasyncflow.png)

- Map async failures cause the stream to abort with a failure

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
    .runWith(Sink.foreach(logToPage))
```

<iframe class="sample" data-src="/samples/map-async-failure"></iframe>
