package playwright.skript.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import playwright.skript.amqp.AMQPManager
import playwright.skript.common.ApplicationVenue
import playwright.skript.venue.JacksonSerializeVenue

class JDBCUserServiceSpec: UserServiceSpec() {
    companion object {
        val objectMapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())

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
        val sqlConnectionProvider by lazy { playwright.skript.venue.JDBCDataSourceVenue(hikariDataSource) }
        val publishVenue by lazy { playwright.skript.stage.AMQPPublishVenue(AMQPManager.amqpExchange, amqpConnection, AMQPManager.basicProperties) }
        val serializeVenue by lazy { JacksonSerializeVenue(objectMapper) }

        val provider: ApplicationVenue by lazy {
            ApplicationVenue(publishVenue, sqlConnectionProvider, serializeVenue)
        }
        val CONSUMER_STAGE: playwright.skript.consumer.alpha.ConsumerStage = playwright.skript.consumer.alpha.AMQPConsumerStage(amqpConnection)
    }



    override fun provider(): ApplicationVenue = provider

    override fun consumerPerformerProvider(): playwright.skript.consumer.alpha.ConsumerStage = CONSUMER_STAGE

    override fun closeResources() {
        hikariDataSource.close()
        amqpConnection.close()
    }
}