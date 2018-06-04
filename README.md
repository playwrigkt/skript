![Build status](https://circleci.com/gh/dgoetsch/skript.svg?style=shield&circle-token=f1ea168988d7d58862e44026aedd74785214c726)
[![latest release](https://img.shields.io/badge/release%20notes-0.0.x-green.svg)](https://github.com/playwrigkt/skript/blob/master/docs/release-notes.md)
[![Bintray](https://api.bintray.com/packages/playwrigkt/skript/skript/images/download.svg)](https://bintray.com/playwrigkt/skript/skript)

# Skript

Describe application logic asynchronously without tying yourself to an underlying
implementation.

This project is in an early stage of development and may change drastically in
order to provider cleaner and more powerful APIs.

## Project Goals

* Concise - Provide a clear, composable API for executing Asynchronous Operations
* Readable - make application semantics a first class citizen
* Modular - Easy to swap out implementations
* Asynchronous - APIs assume non blocking implementation
* Lightweight - Consume minimal system resources
* Fault tolerant - Efficiently escalate and handle errors

## Core API

The [core api](api/core/README.md) provides the base definitions for
the skript apiskript applications. Other projects in [api/](api/)
provide contracts for integrating other tools with skript.

## Examples

Examples can be found in the [examples repository](https://github.com/playwrigkt/skript-examples)

## Motivation

If you're familiar with asynchronous jvm applications, you've probably heard reference to "callback hell".  This project
was first conceptualized as a way to the pain of implementing asynchronous code in java.  Indeed, skripts really are
just a series of callbacks, presented in a clean way.

## Running Tests

1. Start the docker environment (postgres): `$ docker-compose up`
2. run the tests and generate reports: `gradle clean build`

## Coming Soon:
* FileSkript
  * coroutine impl
  * vertx impl
* Config Skript
  *  coroutine impl
  *  vertx impl
  *  runtime update
* AMQPQueue Mangement
* SQL schema management (maynot?)
* CacheSkript
* Encryption
