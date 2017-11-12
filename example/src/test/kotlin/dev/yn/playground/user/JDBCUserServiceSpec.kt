package dev.yn.playground.user

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.common.ApplicationContextProvider
import dev.yn.playground.consumer.alpha.ConsumedMessage
import dev.yn.playground.consumer.alpha.Stream
import dev.yn.playground.coroutine.sql.JDBCDataSourceTaskContextProvider
import dev.yn.playground.publisher.PublishTaskContextProvider
import dev.yn.playground.publisher.PublishTaskExecutor
import dev.yn.playground.sql.context.SQLExecutor
import dev.yn.playground.sql.context.SQLTaskContextProvider
import dev.yn.playground.task.Task
import dev.yn.playground.user.models.UserProfile
import dev.yn.playground.user.models.UserSession
import dev.yn.playground.vertx.publisher.VertxPublishTaskContextProvider
import dev.yn.playground.vertx.task.VertxResult
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import org.funktionale.tries.Try

class JDBCUserServiceSpec: UserServiceSpec() {
    companion object {
        val vertx by lazy { Vertx.vertx() }

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
        val hikariDataSource by lazy { HikariDataSource(hikariDSConfig) }

        val sqlConnectionProvider by lazy { JDBCDataSourceTaskContextProvider(hikariDataSource) as SQLTaskContextProvider<SQLExecutor> }
        val publishContextProvider by lazy { VertxPublishTaskContextProvider(vertx) as PublishTaskContextProvider<PublishTaskExecutor> }
        val provider: ApplicationContextProvider by lazy {
            ApplicationContextProvider(publishContextProvider, sqlConnectionProvider, vertx)
        }
    }

    override fun provider(): ApplicationContextProvider = provider

    override fun closeResources() {
        hikariDataSource.close()
        val future = Future.future<Void>()
        vertx.close(future.completer())
        awaitSucceededFuture(VertxResult(future))
    }

    override fun loginConsumer(): Stream<UserSession> {
        return awaitSucceededFuture(
                userLoginConsumer(provider)
                        .stream(Task
                                .map<ConsumedMessage, JsonObject, ApplicationContext> { JsonObject(String(it.body)) }
                                .mapTry { Try {
                                    UserSession(
                                            it.getString("sessionKey"),
                                            it.getString("userId"),
                                            it.getInstant("expiration")) } }))!!
    }

    override fun createConsumer(): Stream<UserProfile> {
        return awaitSucceededFuture(
                userCreateConsumer(provider)
                        .stream(Task
                                .map<ConsumedMessage, JsonObject, ApplicationContext> { JsonObject(String(it.body)) }
                                .mapTry { Try {
                                    UserProfile(
                                            it.getString("id"),
                                            it.getString("name"),
                                            it.getBoolean("allowPublicMessage")
                                    )
                                } }))!!
    }
}