package dev.yn.playground.user

import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.yn.playground.amqp.AMQPManager
import dev.yn.playground.common.ApplicationContextProvider
import dev.yn.playground.consumer.alpha.AMQPConsumerExecutorProvider
import dev.yn.playground.consumer.alpha.ConsumerExecutorProvider
import dev.yn.playground.context.*
import dev.yn.playground.publisher.PublishSkriptExecutor
import dev.yn.playground.serialize.SerializeSkriptExecutor
import dev.yn.playground.sql.SQLExecutor

class JDBCUserServiceSpec: UserServiceSpec() {
    companion object {
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

        val hikariDataSource by lazy { HikariDataSource(hikariDSConfig) }

        val sqlConnectionProvider by lazy { JDBCDataSourceSkriptContextProvider(hikariDataSource) as SQLSkriptContextProvider<SQLExecutor> }
        val publishContextProvider by lazy { AMQPPublishSkriptContextProvider(AMQPManager.amqpExchange, amqpConnection, AMQPManager.basicProperties) as PublishSkriptContextProvider<PublishSkriptExecutor> }
        val serializeContextProvider = JacksonSerializeSkriptContextProvider() as SerializeSkriptContextProvider<SerializeSkriptExecutor>

        val provider: ApplicationContextProvider by lazy {
            ApplicationContextProvider(publishContextProvider, sqlConnectionProvider, serializeContextProvider)
        }

        val consumerExecutorProvider: ConsumerExecutorProvider = AMQPConsumerExecutorProvider(amqpConnection)
    }

    override fun provider(): ApplicationContextProvider = provider
    override fun consumerExecutorProvider(): ConsumerExecutorProvider = consumerExecutorProvider
    override fun closeResources() {
        hikariDataSource.close()
        amqpConnection.close()
    }


}