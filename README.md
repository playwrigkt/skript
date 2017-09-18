## Vertx SQL Composer

This is an experimental project that provides an interface for composing SQL queries and other asynchronous I/O or synchronous computation into futures and automatically handling transactions.

### Project Goals

This project has one central goal: Reduce the amount of code you have to write to create a vertx application. To do this, I am pursuing the following concrete goals:

* Eliminate Boiler plate code for creating vertx applications
* Provide a clear, DSL-like API for executing Asynchronous I/O
* Allow users to create static tasks that describe application behavior and can be executed as many times as desired at any time after application startup
* Do so without consuming a noticeable amount of system resources

### Core Concepts

1. Unprepared Tasks - static objects do not have the runtime resources required to be executed (for example a SQL connection or Vertx instance).  Thus, the static tasks must be "prepared"
2. Providers - objects that provide runtime objects to unprepared tasks (i.e. database connection or Vertx instance)
3. Tasks - an unprepared task that has been prepared and is ready to run.
4. SQLActionChain - A chain of SQL Queries and Commands which may also include other asynchronous or synchronous actions.  A SQLActionChain may be executed transactionally and the library will handle rolling back on failure.  A SQLActionChain may also allow one to execute other types of I/O as part of the transaction and rollback in case of failure.

### Examples


```
package dev.yn.playground.user

import dev.yn.playground.sql.UnpreparedSQLActionChain
import dev.yn.playground.sql.task.SQLClientProvider
import dev.yn.playground.sql.task.UnpreparedSQLTask
import dev.yn.playground.sql.task.UnpreparedTransactionalSQLTask
import dev.yn.playground.task.*
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.sql.SQLClient
import org.funktionale.tries.Try
import java.time.Instant
import java.util.*

//Mapping code ommitted from example
//Model definitions excluded from example

//Transaction Definitions
object Transactions {
    fun <P: VertxProvider> createUserActionChain(): UnpreparedSQLActionChain<UserProfileAndPassword, UserProfile, P> =
            UnpreparedSQLActionChain.update<UserProfileAndPassword, UserProfileAndPassword, P>(InsertUserProfileMapping)
                    .update(InsertUserPasswordMapping)
                    .mapTask<UserProfile>(UnpreparedVertxTask(VertxTask.sendWithResponse(userCreatedAddress))) //Transactions will be rolled back if vertx publish fails
    
    fun <P: VertxProvider> loginActionChain(): UnpreparedSQLActionChain<UserNameAndPassword, UserSession, P> =
            UnpreparedSQLActionChain.query<UserNameAndPassword, UserIdAndPassword, P>(SelectUserIdForLogin)
                    .query(ValidatePasswordForUserId)
                    .query(EnsureNoSessionExists)
                    .map(createNewSession)
                    .update(InsertSession)
    
    fun <P> getUserActionChain(): UnpreparedSQLActionChain<TokenAndInput<String>, UserProfile, P> =
            validateSession<String, P>(canAccessUser<P>())
                    .query(SelectUserProfileById)
    
    private val createNewSession: (String) -> UserSession = { UserSession(UUID.randomUUID().toString(), it, Instant.now().plusSeconds(3600)) }
    
    private fun <T, P> validateSession(validateSession: (UserSession, T) -> Try<T>): UnpreparedSQLActionChain<TokenAndInput<T>, T, P> =
            UnpreparedSQLActionChain.query(SelectSessionByKey(validateSession))
    
    private fun <P> canAccessUser(): (UserSession, String) -> Try<String> = { session, userId ->
            if (session.userId == userId) {
                Try.Success(userId)
            } else {
                Try.Failure(UserError.AuthorizationFailed)
            }
        }
}

//Provider Implementation
class SQLAndVertxProvider(val vertx: Vertx, val sqlClient: SQLClient) : SQLClientProvider, VertxProvider {
    override fun provideVertx(): Vertx = vertx

    override fun provideSQLClient(): SQLClient{
        return sqlClient
    }
}

//Static unprepared tasks
object UnpreparedTasks {
    //transactional updates
    val unpreparedCreateUserTask: UnpreparedTask<UserProfileAndPassword, UserProfile, SQLAndVertxProvider> =
            UnpreparedTransactionalSQLTask.chain(Transactions.createUserActionChain<SQLAndVertxProvider>())
    
    val unpreparedLoginTask: UnpreparedTask<UserNameAndPassword, UserSession, SQLAndVertxProvider> =
            UnpreparedTransactionalSQLTask.chain(Transactions.loginActionChain<SQLAndVertxProvider>())
                .andThen(UnpreparedVertxTask(VertxTask.sendWithResponse(userLoginAddress))) //Vertx Task is executed outside of transaction and will not impact commit
                
    //Non-transactional query
    val unpreparedGetUserTask: UnpreparedTask<TokenAndInput<String>, UserProfile, SQLAndVertxProvider> =
            UnpreparedSQLTask.chain(Transactions.getUserActionChain<SQLAndVertxProvider>())
}

//Service Implementation
class UserService(val vertx: Vertx, val client: SQLClient) {
    val provider = SQLAndVertxProvider(vertx, client)

    val createUserTask: Task<UserProfileAndPassword, UserProfile> = UnpreparedTasks.unpreparedCreateUserTask.prepare(provider)
    val loginUserTask = UnpreparedTasks.unpreparedLoginTask.prepare(provider)
    val getUserTask: Task<TokenAndInput<String>, UserProfile> = UnpreparedTasks.unpreparedGetUserTask.prepare(provider)

    fun createUser(userProfile: UserProfileAndPassword): Future<UserProfile> = createUserTask.run(userProfile)
    fun login(userNameAndPassword: UserNameAndPassword): Future<UserSession> = loginUserTask.run(userNameAndPassword)
    fun getUser(userId: String, token: String): Future<UserProfile> = getUserTask.run(TokenAndInput(token, userId))
}
```


This code is found in the example submodule, in the package `dev.yn.playground.user`.

TODO:
*  Streaming
