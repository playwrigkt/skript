# Skript Core

This module provides the basis for skript composition and application
runtime.

## Core Concepts

Skript models itself after a play.

* Skript: A skript is a linear graph of one or more actions that are
  executed in a non blocking manner. Skripts implement business logic
  in a (mostly) technology agnostic way.
* Troupe: Each skript has a troupe, a troupe has many performers, which implement
  application resources such as database connections and application configuration
  properties
* Performer: a performer is part of the Stage and provides a specific
  implementation of an application resource
* Venue: A stageManager provides troupes so that Skripts may be run.  A Venue
  should handle initializing and managing application resources.

## What is a skript?

A skript is a set of functions that are run asynchronously and can be
run as many times as desired.  Skripts can be application singletons
that define behavior, static variables, or even single use objects.  You
can think of a skript like a real life script.  A play has a script that
defines what lines an actor should say, and some troupe directions, but
the details of how skripts are actually performed are left up to the cast.

A skript offers the following features:

* High readability
* Composability
* Asynchronous execution
* Short circuit error handling
* Low overhead

One of the simplest skripts possible just transforms an integer to a String.

```
val skript = Skript.map<Int, String, Unit> { it.toString() }

skript.run(10, Unit) shouldBe AsyncResult.succeeded("10")
skript.run(30, Unit) shouldBe AsyncResult.succeeded("30")
```

This example doesn't do much that we don't get out of the box with most
programming languages, so it isn't really showing off what Skripts really
can do. However, its a good place to start examining skript behavior.

In the first line, we create a `map` skript, which simply executes a
synchronous function (we'll get to asynchronous soon).  The skript has
three type parameters: `<Input, Output, Stage>`, input and output are
pretty self explanatory.  This simple example doesn't require a troupe
so the `Unit` value suffices. and we'll get to Stage later.

This skript is essentially a function that takes in type Int and returns
an asynchronous result with a String in it. Since the map skript is
implemented synchronously the result is immediately available,but this
is not always the case with skripts.

The second and third lines run the skript.  For now, ignore the `Unit`
value being passed in, as thats the "troupe.

### Composing skripts

Chaining skripts is one of the most fundamental and powerful features of
skripts.  Skripts are stored in memory as a single-linked list of objects.
For any skript in the chain, its output is the input for the next skript,
until the end of the chain.  The last skript's output type is the output
type of the chain.

Here is a simple example of a chained skript:

```
val skript = Skript
        .map<Int, String, Unit> { it.toString() }
        .map { it.toLong() * 2 }

skript.run(10, Unit).result() shouldBe 20L
skript.run(5, Unit).result() shouldBe 10L
skript.run(200, Unit).result() shouldBe 400L
```

Again, this skript performs a couple of trivial operations.  The first
skript in the chain transforms an int to a String, and the second skript
transforms the string into a long and doubles it.

### Branching skripts

Skripts also offer branching mechanisms.  A simple branching skript may
perform simple mathematical calculations:

```
val double = Skript.map<Int, Int, Unit> { it * 2 }
val half = Skript.map<Int, Int, Unit> { it / 2 }
val rightIfGreaterThanTen = Skript.map<Int, Either<Int, Int>, Unit> {
    if(it > 10) {
        Either.right(it)
    } else {
        Either.left(it)
    }
}


val skript = Skript.branch(rightIfGreaterThanTen)
        .left(double)
        .right(half)

skript.run(5, Unit).result() shouldBe 10
skript.run(16, Unit).result() shouldBe 8
```

The above skript either halves or double, depending on the result of
`rightIfGreaterThanTen`.  `control` returns an `Either`.  When the result
of that skript is an `Either.Right`, then the skript `right` is executed,
otherwise `left` is executed.

### Combining composition and branching

Things get more interesting when you are also mapping values within your
branching logic:

```
val double: Skript<Double, Double, Unit> = Skript.map { it * 2 }
val stringLength = Skript.map<String, Int, Unit> { it.length }
val toLong: Skript<Number, Long, Unit> = Skript.map { it.toLong() }

val rightIfGreaterThanTen = Skript.map<Int, Either<Double, String>, Unit> {
    if(it > 10) {
        Either.right(it.toString())
    } else {
        Either.left(it.toDouble())
    }
}

val skript = Skript.branch(rightIfGreaterThanTen)
        .left(double.andThen(toLong))
        .right(stringLength.andThen(toLong))

skript.run(5, Unit) shouldBe AsyncResult.succeeded(10L)
skript.run(16, Unit) shouldBe AsyncResult.succeeded(2L)
```

This example not only branches, but also transforms before branching.
The logic here can be summarized as follows: if the input is greater than
ten, transform it into a string and return the length as a Long;
otherwise convert the input to a Double, then double the value and
convert it into a long.

The logic that happens in both branches has two steps: transform, and
then convert to a long.  Noticing this, we can take advantage of the
composability of skripts.  The left and right values, both of them call
one skript, and then chain into another:

```
Skript.branch(rightIfGreaterThanTen)
    .left(double.andThen(toLong()))
    .right(stringLength.andThen(toLong()))
```


While this example implements trivial logic it explained several key
concepts: skript branching and skript composition

The next sections will explore how to use these features to implement
more meaningful functionality.

## Parallellized skripts

Two skripts can be run concurrently using `both`

```
val sum: Skript<List<Int>, Int, Unit> = Skript.map { it.sum() }
val length: Skript<List<*>, Int, Unit> = Skript.map { it.size }

val average = Skript.both(sum, length).join { sum, length -> sum.toDouble() / length }

val input = listOf(1, 3, 5, 6, 3)
average.run(input, Unit).result() shouldBe input.average()
```

In this example, sum and length are run, and then the results are put into a Pair.
The join extension doesn't do anything but take sum and length out of their pair,
but it provides a readable way to compose parallellism.

`split` is a special case of `both`.  In a split, the left skript is the identity skript.
`split` essentially allows you to preserve the output from one skript and combine it with
the output of another skript.  One place where split is super useful is when performing an
update that does not return an object, but you still want to use the original input.

```
val initialSkript = Skript.identity<String, Unit>()
val mapping: Skript<String, Int, Unit> = Skript.map { it.length }

val skript = initialSkript
        .split(mapping)
        .join { str, length -> "$str is $length characters long" }

skript.run("abcde", Unit).result() shouldBe "abcde is 5 characters long"
```

## StageManagers, Troupes, and Skripts

Skripts wouldn't be very interesting if they only provided functional composition.

The major benefit of skripts is the api they provide for interacting with application resources.

Each skript has a Troupe.  A Troupe is able to provide serveral performers, all of which
perform a special funktion such as SQL interractions, HTTP client calls, Queue Interractions,
or anything else you can imagine.

The stageManage and Troupe models are written to be modular.  You can choose your own implementation for any of them,
or even write your own.  Currently, there are two implementations for all of the api's provided by skript:
* Vertx (SQL, Queue, Http, Serialization)
* Couroutine JDBC, AMQP, Ktor, and Jackson

There is an example application implemented in the [examples directory](https://github.com/dgoetsch/skript/tree/master/examples/api).

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
* StageManager lifecycle handling
* FileSkript
* CacheSkript
* Config Skript
* Encryption
