# Akka Streams

##### Map Async

<div class="left">
![mapasyncflow](/resources/mapasyncflow.png)
</div>

<div class="right">
<ul>
<li>Simple source of strings</li>
<li>Run async api with parallelism of 2</li>
<li>Lower case letters run slower than upper</li>
</ul>
</div>

<div class="clear"/>

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
  .mapAsync(2)(asyncApi _)
  .runWith(Sink.foreach(logToPage))
```

<iframe class="sample" data-src="/samples/basic-async-ordered"></iframe>
