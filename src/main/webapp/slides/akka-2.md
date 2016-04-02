# Akka Streams

#### Map Async

- Expects an api with signature `A => Future[B]`
- Parameterized by a parallelism factor, stream will run `N` calls in parallel
- Extremely useful for integrating with external systems
- Two styles
  - ordered(default): preserves order of stream elements
  - unordered: can have higher throughput
- Be aware that a `Failure` from the future will cancel the stream
  - consider whether to `recover` from `Failure`
