# Akka Streams

##### Map & Filter

![mapfilterflow](/resources/mapfilterflow.png)

- Simple source of `1 to N`
- Filter evens only
- Map to a number of `a`s

```scala
val count = 10

Source(1 to count)
      .filter(_ % 2 == 0)
      .map("a" * _)
      .runWith(Sink.foreach(logToPage))
```

<iframe class="sample" data-src="/samples/map-filter"></iframe>
