![Build status](https://circleci.com/gh/dgoetsch/skript.svg?style=shield&circle-token=f1ea168988d7d58862e44026aedd74785214c726)

# Skript

This project allows users to implement application logic independently of technology

This project is in an early troupe of development and may change drastically in
order to provider cleaner and more powerful APIs.

## Project Goals

* Concise - Provide a clear, DSL-like API for executing Asynchronous Operations
* Clear - Make application logic clear
* Separation of Concerns - Separate application logic from underlying technology
* Lightweight - Consume minimal system resources
* Interchangeable - skript implementaiton can be swapped out without rewrite
* Fault tolerant - Efficiently escalate and handle errors

## Core API

The [core api](api/core/README.md) provides the base definitions for
the skript apiskript applications. Other projects in [api/](api/)
provide contracts for integrating other tools with skript.

## StageManagers, Troupes, and Skripts

Skripts wouldn't be very interesting if they only provided functional composition.

The major benefit of skripts is the api they provide for interacting with application resources.

Each skript has a Troupe.  A Troupe is able to provide serveral performers, all of which
perform a special funktion such as SQL interractions, HTTP client calls, Queue Interractions,
or anything else you can imagine.

A Troupe is managed by a stageManager.  A stage manager handles the
creation and deletion of application resources such as a network
connection or configuration property.

Currently there are several api's defined in skript that conform to the
core design:
* [http](api/http) -  api for http server and client
* [sql](api/sql) - api for sql interactions
* [serialize](api/serialize) - api for serialization
* [queue](api/queue) - api for queue interactions

A developer should be able to build and test all of their application
logic by only using the classes in the `api` modules.  When it comes time
to run the applicatation, the developer can choose an existing implementation
of an api or write their own, depending on their specific needs.  Currently,
there are two implementations for all of the api's provided by skript:

* Vertx (SQL, Queue, Http, Serialization)
* Couroutine JDBC, AMQP, Ktor, and Jackson

There is an example application implemented in the [examples directory](https://github.com/dgoetsch/skript/tree/master/examples/api).

## Venues and Produktions

Skripts are more than functions that have application resources, but applications need ways to
consume input and initiate skripts.  The Venue and Produktion model defines an API for
running skripts based on a rule.  Venues and Produktions are data sources.  You can think
of a Venue as a particular data source and a Produktion as a consumer of that data.  For
example a message broker and a consumer or an httpServer and an endpoint implementation.

## Extensibility

Skript is exensible.  Any user can implement their own StageManager, Troupe,
and Skripts to provide custom functionality. Additionally a user could choose to
implement their own Venue and Produktion.

It is both possible and planned that more implementations will be made available as the
development of skript progresses.  Currently there are other modules planned such as

* File I/O
* Application Configuration
* Caching

## Use for Load testing and benchmarking

The initial reason this project was started was to create a way to easily
swap technical implementations from an application without having to rewrite
business logic in order to determine which technologies are the best choice
for a given problem.

Such benchmarking has not yet been done, but now that the http server support
has been implemented it is possible to write a rest API and test it with different
technology and not change anything but minor configuration code.


## Running Tests

1. Start the docker environment (postgres): `$ docker-compose up`
2. run the tests: `gradle clean test`

## Coming Soon:
* FileSkript
  * coroutine impl
  * vertx impl
* Config Skript
  *  default impl
  *  coroutine impl
  *  vertx impl
  *  runtime update
* Application lifecycle
  * venue loading
  * production config + starting
* AMQPQueue Mangement
* CacheSkript
* Encryption
