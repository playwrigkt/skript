## Vertx SQL Composer

This is an experimental project that provides an interface for composing SQL
queries into futures and automatically handling transactions.

This project attempts to provide the most valuable functionality found in Slick
(i.e. easily mappable database actions)

The goal of this project is to be able to write database code that is
easy to understand and fullyl asynchronous.  For example:


```
    package dev.yn.playground.user
    
    import dev.yn.playground.sql.SQLTransaction
    import dev.yn.playground.sql.task.SQLClientProvider
    import dev.yn.playground.sql.task.SQLTask
    import dev.yn.playground.task.UnpreparedTask
    import dev.yn.playground.task.VertxProvider
    import dev.yn.playground.task.VertxTask
    import dev.yn.playground.task.vertxAsync
    import io.vertx.core.Future
    import io.vertx.core.Vertx
    import io.vertx.ext.sql.SQLClient
    import java.time.Instant
    import java.util.*
    
    //Application Constants
    val userLoginAddress = "user.login"
    
    val loginTransaction: SQLTransaction<UserNameAndPassword, UserSession> =
            SQLTransaction.query(SelectUserIdForLogin)
                    .query(ValidatePasswordForUserId)
                    .query(EnsureNoSessionExists)
                    .map { UserSession(UUID.randomUUID().toString(), it, Instant.now().plusSeconds(3600)) }
                    .update(InsertSession)
    
    val unpreparedLoginTask: UnpreparedTask<UserNameAndPassword, UserSession, SQLAndVertxProvider> =
            SQLTask.unpreparedTransactionalSql<UserNameAndPassword, UserSession, SQLAndVertxProvider>(loginTransaction)
                    .vertxAsync(VertxTask.sendAndForget(userLoginAddress))
    
    //Provides application instances to tasks
    class SQLAndVertxProvider(val vertx: Vertx, val sqlClient: SQLClient) : SQLClientProvider, VertxProvider {
        override fun provideVertx(): Vertx = vertx
    
        override fun provideSQLClient(): SQLClient{
            return sqlClient
        }
    }
    
    //Service Implementation
    class LoginService(val vertx: Vertx, val client: SQLClient) {
        val provider = SQLAndVertxProvider(vertx, client)
        val loginTask = unpreparedLoginTask.prepare(provider)
    
        fun login(userNameAndPassword: UserNameAndPassword): Future<UserSession> = loginTask.run(userNameAndPassword)
    }
```

This code is found in dev.yn.playground.user.UserTransactions and dev.yn.playground.user.UserService.

TODO:
*  Streaming
