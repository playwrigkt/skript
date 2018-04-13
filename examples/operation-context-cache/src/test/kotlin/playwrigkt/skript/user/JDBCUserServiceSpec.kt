package playwrigkt.skript.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import playwrigkt.skript.amqp.AMQPManager
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.stagemanager.JacksonSerializeStageManager
import playwrigkt.skript.venue.AMQPVenue
import playwrigkt.skript.venue.QueueVenue

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
        val sqlConnectionStageManager by lazy { playwrigkt.skript.stagemanager.JDBCDataSourceStageManager(hikariDataSource) }
        val publishStageManager by lazy { playwrigkt.skript.stagemanager.AMQPPublishStageManager(AMQPManager.amqpExchange, amqpConnection, AMQPManager.basicProperties) }
        val serializeStageManager by lazy { JacksonSerializeStageManager(objectMapper) }

        val stageManager: ApplicationStageManager by lazy {
            ApplicationStageManager(publishStageManager, sqlConnectionStageManager, serializeStageManager)
        }
        val amqpVenue: QueueVenue = AMQPVenue(amqpConnection)
    }



    override fun stageManager(): ApplicationStageManager = stageManager

    override fun queueVenue(): QueueVenue = amqpVenue

    override fun closeResources() {
        hikariDataSource.close()
        amqpConnection.close()
    }
}