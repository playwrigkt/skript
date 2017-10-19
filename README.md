# Vertx SQL Composer

This is an experimental project that provides an interface for composing SQL queries and other asynchronous I/O or synchronous computation into futures and automatically handling transactions.

## Project Goals

This project has one central goal: Reduce the amount of code you have to write to create a vertx application. To do this, I am pursuing the following concrete goals:

* Eliminate Boiler plate code for creating vertx applications
* Provide a clear, DSL-like API for executing Asynchronous I/O
* Make application business logic clear and separate business logic from everything else
* Allow users to create static tasks that describe application behavior and can be executed as many times as desired at any time after application startup
* Do not consuming a noticeable amount of system resources
* Allow flexible interface for database interractions (don't provide opinionated ORM like functionality, i.e. JPA because that isn't hard to do and execution is slow)

## Core Concepts

1. Unprepared Tasks - static objects do not have the runtime resources required to be executed (for example a SQL connection or Vertx instance).  Thus, the static tasks must be "prepared"
2. Providers - objects that provide runtime objects to unprepared tasks (i.e. database connection or Vertx instance)
3. Tasks - an unprepared task that has been prepared and is ready to run.
4. SQLActionChain - A chain of SQL Queries and Commands which may also include other asynchronous or synchronous actions.  A SQLActionChain may be executed transactionally and the library will handle rolling back on failure.  A SQLActionChain may also allow one to execute other types of I/O as part of the transaction and rollback in case of failure.

## What is a task?

A task is a set of functions that are run asynchronously and can be run as many times as desired.  Tasks can be application
singletons that define behavior, static variables, or even single use objects.

A task offers the following features:

* High readability
* Composability
* Asynchronous execution
* Short circuit error handling

Here is a very high level example:

```
val transformToUserIdTask: Task<UsernameAndPassword, UserIdAndPassword>
val loginTask: Task<UserIdAndPassword, Session>
val reportLoginTask: Task<Session, Session>

val apiTask: Task<UsernameAndPassword, Session> = transformToUserIdTask
                .andThen(loginTask)
                .andThen(reportLoginTask)

fun login(input: UsernameAndPassword): Future<Session> {
    return apiTask.run(input)
}
```

This high level example does not address many real world concerns such as
transaction management and the injection of runtime dependencies.

## What is a SQLAction?

SQLAction is an API built on top of tasks.  SQLActions contain SQL commands and queries.  A SQLAction may contain any
number of SQLActions.  A SQLAction may include other asynchronous operations in the form of tasks.

Here is an example of creating a user:

```
val vertx: Vertx

val createUserActionChain: SQLAction<UserProfileAndPassword, UserProfile> =
        SQLAction.update(InsertUserProfileMapping)
                .update(InsertUserPasswordMapping)
                .mapTask(VertxTask.sendWithResponse("user.created", vertx))
```

This method clearly does three things:
1. runs a SQL update called InsertUserProfileMapping
2. runs a second SQL update called InsertUserPasswordMapping
3. runs a vertx task that sends a message to the event bus and listens for a response

In order to really understand whats going on, we will have to look into the SQLMappings..

```
object InsertUserProfileMapping: UpdateSQLMapping<UserProfileAndPassword, UserProfileAndPassword> {
    val insertUser = "INSERT INTO user_profile (id, user_name, allow_public_message) VALUES (?, ?, ?)"

    override fun toSql(i: UserProfileAndPassword): SQLStatement =
            SQLStatement.Parameterized(insertUser, JsonArray(listOf(i.userProfile.id, i.userProfile.name, i.userProfile.allowPubliMessage)))

    override fun mapResult(i: UserProfileAndPassword, rs: UpdateResult): Try<UserProfileAndPassword> =
            if(rs.updated == 1) Try.Success(i) else Try.Failure(SQLError.UpdateFailed(this, i))
}
```

This object provides instructions on how to map to a SQLStatement (query with optional parameters), and how to map from
the resultset to a new object.  We can see here, that the toSql method creates an instance of SQLStatement.Parameterized
which contains an insert query and a list of parameters.  The mapResult method returns the input if one row was updated,
otherwise a failure is returned.  In the event of this failure, no further actions on the chain are run, and the
transaction is rolled back (if there is one).

Even complex sql transactions can be expressed in simple terms with the SQLAction API:

```
val createChatRoomTransaction: SQLAction<TokenAndInput<ChatRoom>, ChatRoom> =
        authenticateSession<ChatRoom>()
                .query(AuthorizeCreateChatroom)
                .map { it.input }
                .mapTry(onlyIfHasUsers)
                .update(InsertChatRoom)
                .update(InsertChatRoomUsers)
                .update(InsertChatRoomPermissions)
                .mapTask(VertxTask.sendWithResponse("chatroom.created", vertx))
```

From a high level, this code is very easy to glance at, and see what is happening. Each line corresponds to a SQLCommand
(`authenticateSession`, `query`, `update`) or an in memory transformation (`mapTry`, `map`).

A task controls connection and transaction management. As mentioned above, a failure in a SQL mapping causes the
entire transaction to roll back, this is because of `dev.yn.playground.sql.task.TransactionalSQLTask` and the features 
of `io.vertx.core.Future`.

Putting the action into a task allows us to easily run the sql transaction:

```
val vertx: Vertx
val sqlClient: SQLClient

val createChatRoomTask: Task<TokenAndInput<ChatRoom>, ChatRoom> =
        TransactionalSQLTask(
                action = authenticateSession<ChatRoom>()
                        .query(AuthrorizeCreateChatroom)
                        .map { it.input }
                        .mapTry(onlyIfHasUsers)
                        .update(InsertChatRoom)
                        .update(InsertChatRoomUsers)
                        .update(InsertChatRoomPermissions)
                        .mapTask(VertxTask.sendWithResponse("chatroom.created", vertx)),
                sqlClient = sqlClient)
                
fun createChatRoom(input: ChatRoom): Future<ChatRoom> {
    return createChatRoomTask.run(input)
}
```

Suppose you want the transaction to commit whether or not the vertx task was completed successfully, its simple:

```
val vertx: Vertx
val sqlClient: SQLClient

val createChatRoomTask: Task<TokenAndInput<ChatRoom>, ChatRoom> =
        TransactionalSQLTask(
                action = authenticateSession<ChatRoom>()
                        .query(AuthrorizeCreateChatroom)
                        .map { it.input }
                        .mapTry(onlyIfHasUsers)
                        .update(InsertChatRoom)
                        .update(InsertChatRoomUsers)
                        .update(InsertChatRoomPermissions),
                sqlClient = sqlClient)
                .andThen(VertxTask.sendWithResponse("chatroom.created", vertx))
```

You may be thinking that these tasks are rather cluttered.  Having the sqlClient and vertx object distracts from the
business logic.  For this reason, there is an "UnpreparedTask" and corresponding "UnpreparedSQLAction".  The goal of
of these objects is to provide a simple, lightweight, and effective means of injecting runtime objects such as a 
database connection or a vertx instance.

## UnpreparedTasks

The main goal of unpreparedTasks is to eliminate the need to recreate tasks, and thus avoid the runtime cost associated
with repeatedly creating task objects.
 
Here is an example of creating an unpreparedTask, notice how this object contains all the instructions for executing
our transaction, but doesn't depend on any actual runtime objects to be instantiated:

```
fun <T> unpreparedAuthenticateSession(): UnpreparedSQLAction<TokenAndInput<T>, SessionAndInput<T>, ApplicationContextProvider> = 
        UnpreparedSQLAction.query(SelectSessionByKey<T>())

val unpreapredCreateChatRoomTask: UnpreparedTask<TokenAndInput<ChatRoom>, ChatRoom, ApplicationContextProvider> =
        UnpreparedTransactionalSQLTask(
                unpreparedAuthenticateSession<ChatRoom>()
                        .query(AuthrorizeCreateChatroom)
                        .map { it.input }
                        .mapTry(onlyIfHasUsers)
                        .update(InsertChatRoom)
                        .update(InsertChatRoomUsers)
                        .update(InsertChatRoomPermissions))
                        .mapTask<ChatRoom>(VertxTask.sendWithResponse("chatroom.created.consumer"))
                .andThen(VertxTask.sendAndForget("chatroom.created.subscriber"))
```

The UnpreparedTasks need to be prepared before they can run.  THis means, provide them with the runtime objects they 
need.  To get a better idea of this, lets look at the implementaiton of ApplicationContextProvider..

```
class ApplicationContextProvider(val vertx: Vertx, val sqlClient: SQLClient) : SQLClientProvider, VertxProvider {
    override fun provideVertx(): Vertx = vertx

    override fun provideSQLClient(): SQLClient = sqlClient
}
```

NOTE: It is up to the user to implement this Provider.

The provider is nothing special.  It is just a container that holds a couple of runtime objects and implements provider interfaces defined by various tasks,
Thanks to the UnpreparedTask interface, we have a simple way of injecting these objects into our tasks:

```
val vertx: Vertx
val sqlClient: SQLClient
val provider = ApplicationContextProvider(vertx, sqlClient)

val createChatRoomTask = unpreapredCreateChatRoomTask.prepare(provider)

fun createChatRoom(input: ChatRoom): Future<ChatRoom> {
    return createChatRoomTask.run(input)
}
```

## Running Tests

1. Start the docker environment (postgres): `$ docker-compose up`
2. run the tests: `gradle clean test`

## TODO:
*  Streaming
