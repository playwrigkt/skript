# Skript

This project allows users to implement application logic independently of technology

This project is in an early stage of development and may change drastically in
order to provider cleaner and more powerful interfaces.

## Project Goals

* Concise - Provide a clear, DSL-like API for executing Asynchronous Operations
* Clear - Make application logic clear
* Separation of Concerns - Separate application logic from underlying technology
* Lightweight - Consume minimal system resources
* Interchangeable - skript implementaiton can be swapped out without rewrite
* Fault tolerant - Efficiently escalate and handle errors

## Core Concepts

Skript models itself after a play.

* Skript: A skript is a list of one or more sequential actions that are
  executed in a non blocking manner. Skripts implement business logic
  in a (mostly) technology agnostic way.
* Stage: Each skript has a stage, a stage has application resources such
  as database connections and application configuration properties
* Performer: a performer is part of the Stage and provides a specific
  implementation of an application resource
* Venue: A venue provides stages so that Skripts may be run.  A Venue
  should handle initializing and managing application resources.

## What is a skript?

A skript is a set of functions that are run asynchronously and can be
run as many times as desired.  Skripts can be application singletons
that define behavior, static variables, or even single use objects.  You
can think of a skript like a real life script.  A play has a script that
defines what lines an actor should say, and some stage directions, but
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
pretty self explanatory.  This simple example doesn't require a stage
so the `Unit` value suffices. and we'll get to Stage later.

This skript is essentially a function that takes in type Int and returns
an asynchronous result with a String in it. Since the map skript is
implemented synchronously the result is immediately available,but this
is not always the case with skripts.

The second and third lines run the skript.  For now, ignore the `Unit`
value being passed in, as thats the "stage.

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

skript.run(10, Unit) shouldBe AsyncResult.succeeded(20L)
skript.run(5, Unit) shouldBe AsyncResult.succeeded(10L)
skript.run(200, Unit) shouldBe AsyncResult.succeeded(400L)
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

skript.run(5, Unit) shouldBe AsyncResult.succeeded(10)
skript.run(16, Unit) shouldBe AsyncResult.succeeded(8)
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

## Stage

The stage is used to inject non static Application resources into a skript.
For example, a database connection or runtime configuration.  Much how a
script is the same regardless of the perfoming cast and venue, a skript
is the same regardless of the underlying implementation. Lets
start by looking at a concrete example.



### SQLSkript

A `SQLSkript` is given its connection through the stage object.  A
SQLSkript is a Skript that is run with a `SQLCast`:

```
sealed class SQLSkript<IN, OUT>: Skript<IN, OUT, SQLCast>
```

A `SQLCast` provides a `SQLPerformer`, there is no need to get into how
that is implemented here, suffice to say an application will use an
implementation of `SQLPerformer` in order to run SQL queries against a
database, without needing to worry about such an implemention while
implementing business logic.


An application that needs a SQL connection might have a stage implemented
like this:

```
data class ApplicationStage(val sqlPerformer: SQLPerformer): SQLCast<SQLPerformer> {
    override fun getSQLPerformer(): SQLPerformer {
        return sqlPerformer
    }
}
```

This class is nothing more than a container for some application objects
that are not available at compile time, and cursory to the application's
business domain.

Most modern web applications will have some notion of a user profile, an
extremely simple example might be as follows:

```
data class UserProfile(val id: String, val name: String, val allowPublicMessage: Boolean)
```

As part of our application, we will probably expose a skript like this:

```
val getUserProfileByIdSkript: Skript<String, UserProfile, ApplicationStage>
```

This skript will handle getting the user.  The underlying implementation
could be swapped out in the future, but for now we decide to implement
as a simple SQL Query:

```
val getUserProfileByIdSkript: Skript<String, UserProfile, ApplicationStage> =
    SQLSkript.query<String, UserProfile, ApplicationStage>(SelectUserProfileById)

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
val stage: ApplicationStage = ...

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
val getUserById = SQLSkript.query<String, UserProfile, ApplicationStage>(SelectUserProfileById)

val result: AsyncResult<UserProfile> = getUserById.run("id1234", stage)
```

This code doesn't provide any actual sql implementation, and it doesn't
provide any sort of runtime objects.  It defines application behavior
and provides a skript to be used when composing transactions.

You are probably thinking right about now that SQL implementations are
pretty easy to swap out in java, assuming that you are okay with using
JDBC. This is true, however the Skript library does something a little
more powerful than allowing you to swap out SQL implementaitons. This
section will show how to use other skript features such as composition
and branching for complex sql logic, how to use non JDBC SQL
implementations, and will show how this technical agnosticism may be
generalized to other types of technologies such as event publishing or
consumer-like processes.

### Composing SQL Transactions

Lets dig into a complex example. This is taken from the
`operation-context-cache` example:

```
val CREATE_CHAT_ROOM_SKRIPT: Skript<ChatRoom, ChatRoom, ApplicationStage<UserSessionProps>> =
       SQLTransactionSkript.transaction(
               AuthSkripts.authenticate<ChatRoom, UserSessionProps>()
                       .mapTry(onlyIfHasUsers)
                       .update(InsertChatRoom)
                       .update(InsertChatRoomUsers)
                       .update(InsertChatRoomPermissions))
```

Just by reading this code, the high level logic is very clear:

* Within a transaction, do the following:
    * first authenticate the request
    * then only continue if there are users in the chatroom
    * then Insert the chatroom
    * then insert the chatroom users
    * then insert the chatroom permissions

Without necessarily understading the underlying implementation, we have
a pretty good idea of how this is implemented.  One can dig into the
sql and very easily understand exactly which queries are being run.

Things get a little more interesting when updating:

```
val UPDATE_CHAT_ROOM_SKRIPT: Skript<ChatRoom, ChatRoom, ApplicationStage<ChatroomOperationProps>> =
        SQLTransactionSkript.transaction(
                AuthSkripts.validate<ChatRoom, ChatroomOperationProps>()
                        .andThen(ChatroomPropsSkripts.hydrateExistingChatroom<ChatRoom>())
                        .andThen(authorizeUser(ChatRoomPermissionKey.Update))
                        .update(UpdateChatRoomFields)
                        .map { it.id }
                        .query(GetChatRoom))
```

This example makes a little more interesting use of ApplicationStage.
When running complex transactions, you often need to use some data that
is not directly related to the Input and Output of the API.  In this case,
the Skript needs to use an existing Chatroom.  One option is to have an input
type that includes all the extant information needed by a Skript
(see the `api` example), another option is to use some `StageProps`,
just like on a real life stage, `StageProps` are objects that help
perform a skript.  In this case, we're saving an existing chatroom to `Props`.

Looking at the implementation of `hydrateExistingChatroom`, we can see
that it reads a chatroom from the database, and adds it to the Stage.

```
fun <I: ChatroomId> hydrateExistingChatroom(): Skript<I, I, ApplicationStage<ChatroomOperationProps>> =
       Skript.updateStage(
               Skript.identity<I, ApplicationStage<ChatroomOperationProps>>()
                       .map { it.getChatroomId() }
                       .query(GetChatRoom)
                       .mapWithStage { chatroom, stage -> stage.getStageProps().useChatroom(chatroom) })
```

`updateStage` is a special task that doesn't transform a task it is
chained to and only operates on the stage.  In this example, the StageProps
are mutable.

### Swapping out implementations

This section will describe how to actually run a skript in a Stage.  The
code here is from the tests for the examples.  Those tests actually implement
an application in two different ways, and run the exact same test suite
against them. To prove that the business logic can be ported between
different technologies.

In a real application, an Application is likely going to have a lot of
components, not just SQL.  An example might look something like this:

```
class ApplicationStage<R>(
        private val publishPerformer: PublishPerformer,
        private val sqlPerformer: SQLPerformer,
        private val serializePerformer: SerializePerformer,
        private val cache: R):
        PublishCast,
        SQLCast,
        SerializeCast,
        StageProps<R>
{
    override fun getSerializePerformer(): SerializePerformer = serializePerformer
    override fun getStageProps(): R = cache
    override fun getPublishPerformer(): PublishPerformer = publishPerformer
    override fun getSQLPerformer(): SQLPerformer = sqlPerformer
}
```

This `ApplicationStage` can run `Publish`, `SQL`, and `Serialize` scripts.
It also provides a `Skript` scoped cache via `StageProps`.

In `Skript`, a `Venue` provides a stage.  A given stage should be used
exactly once, but a `Venue` should live for the entire application lifecycle
and provide a `Stage` each time a `Skript` is invoked.

The `Venue` interface itself is still a Work in Progress, and is likely to change:

```
class ApplicationVenue (
        val publishVenue: Venue<PublishPerformer>,
        val sqlVenue: Venue<SQLPerformer>,
        val serializeVenue: Venue<SerializePerformer>
): Venue<ApplicationStage<Unit>> {

    override fun provideStage(): AsyncResult<ApplicationStage<Unit>> = provideStage(Unit)

    fun <R> provideStage(r: R): AsyncResult<ApplicationStage<R>> {
        return sqlVenue.provideStage().flatMap { sqlPerformer ->
                    publishVenue.provideStage().flatMap { publishPerformer ->
                        serializeVenue.provideStage().map { serializePerformer ->
                            ApplicationStage(publishPerformer, sqlPerformer, serializePerformer, r)
                        }
                    }
                }
    }

    fun <I, O, R> runOnStage(skript: Skript<I, O, ApplicationStage<R>>, i: I, r: R): AsyncResult<O> {
        return provideStage(r)
                .flatMap { skript.run(i, it) }
    }
}
```

Now we can actually have code that uses the venue to run tasks:

```
fun createChatRoom(chatRoom: ChatRoom, sessionKey: String): AsyncResult<ChatRoom> =
        venue.runOnStage(ChatRoomSkripts.CREATE_CHAT_ROOM_SKRIPT, chatRoom, ChatroomStageProps(sessionKey))

fun updateChatRoom(chatRoom: ChatRoom, sessionKey: String): AsyncResult<ChatRoom> =
        venue.runOnStage(ChatRoomSkripts.UPDATE_CHAT_ROOM_SKRIPT, chatRoom, ChatroomStageProps(sessionKey))
```

At this point, we've wired everything up, but we still haven't created
a venue.

The tests provide two main examples.

#### Vertx Implementation

The first example uses the Vertx SQL interface, Vertx Json serialization,
and the Vertx event bus for publishing and consuming.

```
val sqlConfig = JsonObject()
        .put("provider_class", "io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider")
        .put("jdbcUrl", "jdbc:postgresql://localhost:5432/chitchat")
        .put("username", "chatty_tammy")
        .put("password", "gossipy")
        .put("driver_class", "org.postgresql.Driver")
        .put("maximumPoolSize", 30)
        .put("poolName", "test_pool")

val vertx by lazy { Vertx.vertx() }

val sqlClient: SQLClient by lazy {
    JDBCClient.createShared(vertx, sqlConfig, "test_ds")
}
val sqlConnectionProvider = VertxSQLVenue(sqlClient)
val publishVenue = VertxPublishVenue(vertx)
val serializeVenue = VertxSerializeVenue()
val provider: ApplicationVenue by lazy {
    ApplicationVenue(publishVenue, sqlConnectionProvider, serializeVenue)
}
```

#### JDBC + AMQP Implementation

The second example uses JDBC and AMQP.  To change this implementaiton,
we dont' have to reimplement any of the `Skripts`, we just have to change
the venue object that we use to generate `Stage` objects.

```
val amqpConnectionFactory: ConnectionFactory by lazy {
    AMQPManager.connectionFactory()
}

val amqpConnection by lazy {
    AMQPManager.cleanConnection(amqpConnectionFactory)
}

val hikariDSConfig: HikariConfig by lazy {
    val config = HikariConfig()
    config.jdbcUrl = "jdbc:postgresql://localhost:5432/chitchat"
    config.username = "chatty_tammy"
    config.password = "gossipy"
    config.driverClassName = "org.postgresql.Driver"
    config.maximumPoolSize = 30
    config.poolName = "test_pool"
    config
}

val hikariDataSource = HikariDataSource(hikariDSConfig)
val sqlConnectionProvider = playwright.skript.venue.JDBCDataSourceVenue(hikariDataSource)
val publishVenue by lazy { AMQPPublishVenue(AMQPManager.amqpExchange, JDBCUserServiceSpec.amqpConnection, AMQPManager.basicProperties) }
val serializeVenue = JacksonSerializeVenue()
val provider: ApplicationVenue by lazy {
    ApplicationVenue(publishVenue, sqlConnectionProvider, serializeVenue)
}
```

Both of the above examples provide an application that implements the
same business logic, but using different technology.

## Running Tests

1. Start the docker environment (postgres): `$ docker-compose up`
2. run the tests: `gradle clean test`

## TODO:
*  DSL
* Application level caching
* FileSkript
* HttpSocketConsumer
