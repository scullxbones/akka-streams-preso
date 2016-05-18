# Back pressure

#### Why is it important?

![Homer the consumer](/resources/homerconsumer.gif)

- Not every producer/consumer pair can be as well matched
- Slow producer, fast consumer - not really needed
  - "Push" based backpressure
- Fast producer, slow consumer - backpressure becomes a requirement
  - "Pull" based backpressure
