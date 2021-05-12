# R2DBC EventStore [![R2DBC EventStore](https://github.com/daggerok/r2dbc-event-store/actions/workflows/ci.yaml/badge.svg)](https://github.com/daggerok/r2dbc-event-store/actions/workflows/ci.yaml)
Checkpoint event-sourced CQRS EventStore application based on Spring Boot and Spring WebFlux with R2DBC, Postgres and nkonev r2dbc migration tool

## Getting started

### test

```bash
./mvnw clean test
```

### run

```bash
./mvnw -P pg-start
./mvnw spring-boot:run
curl -iv 0:8080/event-stream
curl -sS 0:8080/append-event -H'Content-Type:application/json' -d'{"aggregateId":"00000000-0000-0000-0000-000000000001","eventType":"VisitorRegisteredEvent","name":"Test visitor"}'
curl -sS 0:8080/append-event -H'Content-Type:application/json' -d'{"aggregateId":"00000000-0000-0000-0000-000000000001","eventType":"PassCardDeliveredEvent"}'
curl -sS 0:8080/append-event -H'Content-Type:application/json' -d'{"aggregateId":"00000000-0000-0000-0000-000000000001","eventType":"EnteredTheDoorEvent","doorId":"IN-1"}'
curl -sS 0:8080/append-event -H'Content-Type:application/json' -d'{"aggregateId":"00000000-0000-0000-0000-000000000001","eventType":"EnteredTheDoorEvent","doorId":"IN-2"}'
curl -sS 0:8080/append-event -H'Content-Type:application/json' -d'{"aggregateId":"00000000-0000-0000-0000-000000000001","eventType":"EnteredTheDoorEvent","doorId":"OUT-2"}'
```

## RTFM

* https://projectreactor.io/docs/core/release/reference/#processors

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.4.4/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.4.4/maven-plugin/reference/html/#build-image)
* [Spring Configuration Processor](https://docs.spring.io/spring-boot/docs/2.4.4/reference/htmlsingle/#configuration-metadata-annotation-processor)
* [Spring Data R2DBC](https://docs.spring.io/spring-boot/docs/2.4.4/reference/html/spring-boot-features.html#boot-features-r2dbc)

### Guides
The following guides illustrate how to use some features concretely:

* [Acessing data with R2DBC](https://spring.io/guides/gs/accessing-data-r2dbc/)

### Additional Links
These additional references should also help you:

* [R2DBC Homepage](https://r2dbc.io)

