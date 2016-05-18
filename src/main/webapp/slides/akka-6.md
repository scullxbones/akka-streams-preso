# Akka Streams

##### Applied - Requirements and Assumptions

- Assume time-series device data in Cassandra (communication satellites? Google Nests? weather stations?)
  - Data expires after TTL
  - Requirements:
    - Aggregate by device and attribute for time intervals
    - Archive for later exploratory analytics
  - Fields
    - Timestamp
    - Device identifier
    - Device attribute
    - Attribute value

