# Akka Streams

##### Simple flow

![basicflow](/resources/basicflow.png)

- Simple source of `1 to N`, with a Sink logging each to console

```scala
val count = 5

Source(1 to count)
    .runWith(Sink.foreach(logToPage))
```

<iframe class="sample" data-src="/samples/simple"></iframe>
