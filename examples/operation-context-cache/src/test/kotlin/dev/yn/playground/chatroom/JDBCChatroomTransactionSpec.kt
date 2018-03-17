package dev.yn.playground.chatroom

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.yn.playground.common.ApplicationContextProvider
import dev.yn.playground.coroutine.sql.JDBCDataSourceTaskContextProvider
import dev.yn.playground.publisher.PublishTaskContextProvider
import dev.yn.playground.publisher.PublishTaskExecutor
import dev.yn.playground.sql.context.SQLExecutor
import dev.yn.playground.sql.context.SQLTaskContextProvider
import dev.yn.playground.vertx.publisher.VertxPublishTaskContextProvider
import dev.yn.playground.vertx.task.VertxResult
import io.vertx.core.Future
import io.vertx.core.Vertx

class JDBCChatroomTransactionSpec: ChatroomTransactionsSpec() {

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

        val hikariDataSource = HikariDataSource(hikariDSConfig)
        val sqlConnectionProvider = JDBCDataSourceTaskContextProvider(hikariDataSource) as SQLTaskContextProvider<SQLExecutor>
        val publishContextProvider = VertxPublishTaskContextProvider(vertx) as PublishTaskContextProvider<PublishTaskExecutor>
        val provider: ApplicationContextProvider by lazy {
            ApplicationContextProvider(publishContextProvider, sqlConnectionProvider)
        } }

    override fun provider(): ApplicationContextProvider = JDBCChatroomTransactionSpec.provider

    override fun closeResources() {
        hikariDataSource.close()
        val future = Future.future<Void>()
        vertx.close(future.completer())
        awaitSucceededFuture(VertxResult(future))
    }
}