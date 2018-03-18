# Vertx SQL Composer

This project allows users to implement application logic independently of technology

## Project Goals

* Concise - Provide a clear, DSL-like API for executing Asynchronous Operations
* Clear - Make application logic clear
* Separation of Concerns - Separate application logic from underlying technology
* Lightweight - Consume minimal system resources
* Interchangeable - skript implementaiton can be swapped out without rewrite
* Fault tolerant - Efficiently escalate and handle errors

## Core Concepts

* Skript: A skript is a list of one or more sequential actions that are executed in a non blocking manner. Everything is a skript.
* Context: Each skript has a context, a context has application resources such as database connections and application configuration properties

## What is a skript?

A skript is a set of functions that are run asynchronously and can be run as many times as desired.  Skripts can be application
singletons that define behavior, static variables, or even single use objects.

A skript offers the following features:

* High readability
* Composability
* Asynchronous execution
* Short circuit error handling

One of the simplest skripts possible just transforms an integer to a String.

```
val skript = Skript.map<Int, String, Unit> { it.toString() }
skript.run(10, Unit) shouldBe AsyncResult.succeeded("10")
skript.run(30, Unit) shouldBe AsyncResult.succeeded("30")
```

This example doesn't do much that we don't get out of the box with most programming languages, so it isn't really showing off what Skripts really can do. However, its a good place to start
examining exactly what a skript is.

In the first line, we create a `map` skript, which simply executes a synchronous function (we'll get to asynchronous soon).  The skript has three type parameters: `<Input, Output, Context>`, input and output are pretty self explanatory and we'll get to context later.
This skript is essentially a function that takes in type Int and returns an asynchronous result with a String in it. Since the map skript is implemented synchronously the result is immediately available,
but this is not always the case with skripts.

The second and third lines run the skript.  For now, ignore the `Unit` value being passed in (we'll get to Context later).

### Composing skripts

Chaining skripts is one of the most fundamental and powerful features of skripts.  Skripts are stored in memory as a single-linked list of objects.
For any skript in the chain, its output is the input for the next skript, until the end of the chain.  The last skripts output type is the output type of the chain.

Here is a simple example of a chained skript:

```
val skript = Skript
        .map<Int, String, Unit> { it.toString() }
        .map { it.toLong() * 2 }
skript.run(10, Unit) shouldBe AsyncResult.succeeded(20L)
skript.run(5, Unit) shouldBe AsyncResult.succeeded(10L)
skript.run(200, Unit) shouldBe AsyncResult.succeeded(400L)
```

Again, this skript performs a couple of trivial operations.  The first skript in the chain transforms an int to a String, and the second skript transforms the string into a long and doubles it.

### Branching skripts

Skripts also offer branching mechanisms.  A simple branching skript may perform simple mathematical calculations:

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

skript.run(5, Unit) shouldBe AsyncResult.succeeded(10)
skript.run(16, Unit) shouldBe AsyncResult.succeeded(8)
```

The above skript either halves or double, depending on the result of `rightIfGreaterThanTen`.  `control` returns an `Either`.  When the result of that skript is an `Either.Right`,
then the skript `right` is executed, otherwise `left` is executed.

### Combining composition and branching

Things get more interesting when you are also mapping values within your branching logic:

```
val double: Skript<Double, Double, Unit> = Skript.map { it * 2 }
val stringLength = Skript.map<String, Int, Unit> { it.length }
fun<N: Number> toLong(): Skript<N, Long, Unit> = Skript.map { it.toLong() }

val rightIfGreaterThanTen = Skript.map<Int, Either<Double, String>, Unit> {
    if(it > 10) {
        Either.right(it.toString())
    } else {
        Either.left(it.toDouble())
    }
}

val skript = Skript.branch(rightIfGreaterThanTen)
    .left(double.andThen(toLong()))
    .right(stringLength.andThen(toLong()))

skript.run(5, Unit) shouldBe AsyncResult.succeeded(10L)
skript.run(16, Unit) shouldBe AsyncResult.succeeded(2L)
```

This example not only branches, but also transforms before branching.  The logic here can be summarized as follows: if the input is greater than ten,
transform it into a string and return the length as a Long; otherwise convert the input to a Double, then double the value and convert it into a long.

The logic that happens in both branches has two steps: transform, and then convert to a long.  Noticing this, we can take advantage of the composability of skripts.
Notice the left and right values, both of them call one skript, and then chain into another:

```
Skript.branch(rightIfGreaterThanTen)
    .left(double.andThen(toLong()))
    .right(stringLength.andThen(toLong()))
```


While this example implements trivial logic it explained several key concepts: skript branching and skript composition

The next sections will explore how to use these features to implement more meaningful interfaces.

## Skript Context

The skript context is used to inject non static Application resources into a skript.  For example, a database connection or runtime configuration.  The context is the main abstraction
for technical implementation.  Lets start by looking at a concrete example.

### SQLSkript

A `SQLSkript` is given its connection through the context object.  A SQLSkript is defined as having a context
that can provide a SQLConnection:

```
sealed class SQLSkript<IN, OUT, C: SQLSkriptContext<*>>: Skript<IN, OUT, C>
```

A SQLSkriptContext provides a method that returns a `SQLPerformer`, there is no need to get into how that is
implemented here, suffice to say an application will use an implementation of SQLPerformer in order to provide
SQL functionality at runtime, without interfering with the skript impelementation.


An application that needs a SQL connection might have a context implemented like this:

```
data class ApplicationContext(val sqlPerformer: SQLPerformer): SQLSkriptContext<SQLPerformer> {
    override fun getSQLPerformer(): SQLPerformer {
        return sqlPerformer
    }
}
```

This class is nothing more than a container for some application objects that are not available at compile time,
and cursory to the application domain.

Most modern web applications will have some notion of a user profile, an extremely simple example might be as follows:

```
data class UserProfile(val id: String, val name: String, val allowPublicMessage: Boolean)
```

As part of our application, we will probably expose a skript like this:

```
val getUserProfileByIdSkript: Skript<String, UserProfile, ApplicationContext>
```

This skript will handle getting the user.  The underlying implementation could be swapped out in the future,
but for now we decide to implement as a simple SQL Query:

```
val getUserProfileByIdSkript: Skript<String, UserProfile, ApplicationContext> = SQLSkript.query<String, UserProfile, ApplicationContext>(SelectUserProfileById)

object SelectUserProfileById: SQLQueryMapping<String, UserProfile> {
  val selectUser = "SELECT id, user_name, allow_public_message FROM user_profile where id = ?"

  ...
}
```

SQLSkripts provide a mapping based interface. This interface allows users to provide instructions on how to transform
the skript input toSql, and then how to map the resultSet.

Mapping to sql defines the query string and parameter binding:

```
object SelectUserProfileById: SQLQueryMapping<String, UserProfile> {
  ...

  override fun toSql(i: String): SQLCommand.Query = SQLCommand.Query(SQLStatement.Parameterized(selectUser, listOf(i)))

  ...
}
```

And mapping to a result defines how to convert the resultSet to the resul type.  In this case we expect exactly one row to be returned:

```
object SelectUserProfileById: SQLQueryMapping<String, UserProfile> {
  ...

  override fun mapResult(i: String, rs: SQLResult.Query): Try<UserProfile> =
           Try { rs.result.next() }
                   .rescue { Try.Failure(IllegalArgumentException("no such user")) }
                   .map {
                       UserProfile(it.getString("id"),
                               it.getString("user_name"),
                               it.getBoolean("allow_public_message")) }
}
```

Putting it all together, and running the skript:

```
val context: ApplicationContext = ...

data class UserProfile(val id: String, val name: String, val allowPublicMessage: Boolean)

object SelectUserProfileById: SQLQueryMapping<String, UserProfile> {
    val selectUser = "SELECT id, user_name, allow_public_message FROM user_profile where id = ?"
    override fun toSql(i: String): SQLCommand.Query = SQLCommand.Query(SQLStatement.Parameterized(selectUser, listOf(i)))

    override fun mapResult(i: String, rs: SQLResult.Query): Try<UserProfile> =
            Try { rs.result.next() }
                    .rescue { Try.Failure(IllegalArgumentException("no such user")) }
                    .map {
                        UserProfile(it.getString("id"),
                                it.getString("user_name"),
                                it.getBoolean("allow_public_message")) }

}
val getUserById = SQLSkript.query<String, UserProfile, ApplicationContext>(SelectUserProfileById)

val result: AsyncResult<UserProfile> = getUserById.run("id1234", context)
```

This code doesn't provide any actual sql implementation, and it doesn't provide any sort of runtime objects.
It defines application behavior and provides a skript to be used when composing transactions.

You are probably thinking right about now that SQL implementations are pretty easy to swap out in java,
assuming that you are okay with  using JDBC.  This is true, however the Skript library does something a little
more powerful than allowing you to swap out SQL implementaitons.  This sectiono will show how to use other
skript features such as composition and branching for complex sql logic, how to use non JDBC SQL
implementations, and will show how this technical agnosticism may be generalized to other types of
technologies such as event publishing or consumer-like processes.

### Composing SQL Transactions




## Running Tests

1. Start the docker environment (postgres): `$ docker-compose up`
2. run the tests: `gradle clean test`

## TODO:
*  Streaming
