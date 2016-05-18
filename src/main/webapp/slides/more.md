# More to explore

#### Some more topics I didn't have time for

- Actor integration
  - No backpressure: `Source.actorRef`, `Sink.actorRef`
  - Backpressure: `Sink.actorRefWithAck`, `ActorPublisher`, and `ActorSubscriber`
  - Remote actors are not allowed in streams due to inherent unreliability of remote calls
- Nesting and flattening stages
- Time windowing and time based `xyzWithin` stages
- Rate management with `conflate`, `expand`, `batch` and `buffer`
- Streams TestKit
- I/O - Tcp, Files, `ByteString`, and the quite useful `Framing`

- [Great reference / cheat-sheet](http://doc.akka.io/docs/akka/current/scala/stream/stages-overview.html)

- Akka Http
