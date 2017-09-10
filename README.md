## Vertx SQL Composer

This is an experimental project that provides an interface for composing SQL
queries into futures and automatically handling transactions.

This project attempts to provide the most valuable functionality found in Slick
(i.e. easily mappable database actions)

The goal of this project is to be able to write database code that is
easy to understand and fullyl asynchronous.  For example:


```
    private val client: io.vertx.ext.sql.SQLClient
    private val sqlTransactionExecutor: SQLTransactionExecutor = SQLTransactionExecutor(client)
    
    private val loginTransaction: SQLTransaction<UserNameAndPassword, UserIdAndPassword, UserSession> =
        query(SelectUserIdForLogin)
            .query(ValidatePasswordForUserId)
            .query(EnsureNoSessionExists)
            .map(createNewSessionKey)
            .update(InsertSession)
    
    fun login(userNameAndPassword: UserNameAndPassword): Future<UserSeesion> = sqlTransactionExecutor.update(userNameAndPassword, loginTransaction)
```

This code is found in dev.yn.playground.user.UserTransactions and dev.yn.playground.user.UserService.

TODO:
*  Streaming
