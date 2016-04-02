# Akka Streams

##### Map Async Unordered

- Simple source of strings
- Run async api with parallelism of 2
- Lower case letters run slower than upper
- Unordered means that upper case letters can get far ahead

```scala
def asyncApi(str: String): Future[String] = Future {
    if (str.nonEmpty && str.head.isLower) {
        Thread.sleep(4 * Random.nextInt(250) + 1000)
    } else {
        Thread.sleep(Random.nextInt(100) + 100)
    }
    str
}


val ids = "a" :: "B" :: "C" :: "D" :: "e" :: "F" :: "g" :: "H" :: "I" :: Nil

Source(ids)
    .mapAsyncUnordered(2)(asyncApi _)
    .runWith(Sink.foreach(logToPage))
```

<iframe class="sample" data-src="/samples/basic-async-unordered"></iframe>
