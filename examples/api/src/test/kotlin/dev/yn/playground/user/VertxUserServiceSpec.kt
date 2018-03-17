package dev.yn.playground.user

import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.common.ApplicationContextProvider
import dev.yn.playground.consumer.alpha.ConsumedMessage
import dev.yn.playground.consumer.alpha.ConsumerExecutorProvider
import dev.yn.playground.consumer.alpha.Stream
import dev.yn.playground.publisher.PublishTaskContextProvider
import dev.yn.playground.publisher.PublishTaskExecutor
import dev.yn.playground.sql.context.SQLExecutor
import dev.yn.playground.sql.context.SQLTaskContextProvider
import dev.yn.playground.task.Task
import dev.yn.playground.user.models.UserProfile
import dev.yn.playground.user.models.UserSession
import dev.yn.playground.vertx.alpha.consumer.VertxConsumerExecutorProvider
import dev.yn.playground.vertx.publisher.VertxPublishTaskContextProvider
import dev.yn.playground.vertx.sql.VertxSQLTaskContextProvider
import dev.yn.playground.vertx.task.VertxResult
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLClient
import org.funktionale.tries.Try

class VertxUserServiceSpec: UserServiceSpec() {
    companion object {
        val vertx by lazy { Vertx.vertx() }

        val hikariConfig = JsonObject()
                .put("provider_class", "io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider")
                .put("jdbcUrl", "jdbc:postgresql://localhost:5432/chitchat")
                .put("username", "chatty_tammy")
                .put("password", "gossipy")
                .put("driver_class", "org.postgresql.Driver")
                .put("maximumPoolSize", 30)
                .put("poolName", "test_pool")
        val sqlClient: SQLClient by lazy {
            JDBCClient.createShared(vertx, hikariConfig, "test_ds")
        }

        val sqlConnectionProvider by lazy { VertxSQLTaskContextProvider(sqlClient) as SQLTaskContextProvider<SQLExecutor> }
        val publishContextProvider by lazy { VertxPublishTaskContextProvider(vertx) as PublishTaskContextProvider<PublishTaskExecutor> }
        val provider: ApplicationContextProvider by lazy {
            ApplicationContextProvider(publishContextProvider, sqlConnectionProvider, vertx)
        }

        val consumerExecutorProvider = VertxConsumerExecutorProvider(vertx)
    }

    override fun provider(): ApplicationContextProvider = VertxUserServiceSpec.provider
    override fun consumerExecutorProvider(): ConsumerExecutorProvider = consumerExecutorProvider
    override fun closeResources() {
        val clientF = Future.future<Void>()
        sqlClient.close(clientF.completer())
        awaitSucceededFuture(VertxResult(clientF))
        val future = Future.future<Void>()
        vertx.close(future.completer())
        awaitSucceededFuture(VertxResult(future))

    }
}