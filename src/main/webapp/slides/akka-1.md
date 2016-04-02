# Akka Streams

- Introduced as a new project with `akka-http`
- Non-experimental, versioned with rest of akka as of `2.4.2`
- Implemented internally with Akka Actors
- Extremely composable, [unlike typical actor messaging](http://eng.localytics.com/akka-streams-akka-without-the-actors/)
  - Three primary shapes:
    - Source: Produces data stream
    - Flow: Transforms data stream
    - Sink: Consumes data stream
  - BidiFlow: A bi-directional flow shape
  - Graph: A generic flow shape built with the GraphDSL
- Fully formed (Closed) shape materialized into an actor network with `implicit ActorMaterializer`
- Closed flows parameterized by materialized type, for type `Mat`, using `Source.runWith(Sink)` yields a `Future[Mat]`
