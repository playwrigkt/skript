package dev.yn.playground.user

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.yn.playground.amqp.alpha.consumer.AMQPConsumerExecutorProvider
import dev.yn.playground.amqp.publisher.AMQPPublishTaskContextProvider
import dev.yn.playground.common.ApplicationContextProvider
import dev.yn.playground.consumer.alpha.ConsumerExecutorProvider
import dev.yn.playground.coroutine.sql.JDBCDataSourceTaskContextProvider
import dev.yn.playground.publisher.PublishTaskContextProvider
import dev.yn.playground.publisher.PublishTaskExecutor
import dev.yn.playground.sql.context.SQLExecutor
import dev.yn.playground.sql.context.SQLTaskContextProvider

class JDBCUserServiceSpec: UserServiceSpec() {
    companion object {
        fun initAMQPResources(connection: Connection) {
            val channel = connection.createChannel()
            val exchange = channel.exchangeDeclare(amqpExchange, BuiltinExchangeType.TOPIC)
            val loginQueue = channel.queueDeclare(userLoginAddress, true, false, false, emptyMap())
            val createQueue = channel.queueDeclare(userCreatedAddress, true, false, false, emptyMap())
            channel.queueBind(userLoginAddress, amqpExchange, userLoginAddress)
            channel.queueBind(userCreatedAddress, amqpExchange, userCreatedAddress)
            channel.close()
        }

        fun destroyAMQPResources(connection: Connection) {
            val channel = connection.createChannel()
            channel.queueDelete(userLoginAddress)
            channel.queueDelete(userCreatedAddress)
            channel.exchangeDelete(amqpExchange)
            channel.close()
        }


        val amqpExchange = "events.ex"
        val basicProperties: AMQP.BasicProperties = AMQP.BasicProperties()
        val amqpConnectionFactory: ConnectionFactory by lazy {
            val factory = ConnectionFactory()
            factory.username = "rabbitmq"
            factory.password = "rabbitmq"
            factory.host = "localhost"
            factory.port = 5672
            factory.virtualHost = "/"
            factory
        }

        val amqpConnection by lazy {
            val connection = amqpConnectionFactory.newConnection()
            destroyAMQPResources(connection)
            initAMQPResources(connection)
            connection
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
        val sqlConnectionProvider by lazy { JDBCDataSourceTaskContextProvider(hikariDataSource) as SQLTaskContextProvider<SQLExecutor> }
        val publishContextProvider by lazy { AMQPPublishTaskContextProvider(amqpExchange, amqpConnection, basicProperties) as PublishTaskContextProvider<PublishTaskExecutor> }

        val provider: ApplicationContextProvider by lazy {
            ApplicationContextProvider(publishContextProvider, sqlConnectionProvider)
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