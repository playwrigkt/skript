package playwrigkt.skript.user

import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import playwright.skript.venue.QueueVenue
import playwrigkt.skript.amqp.AMQPManager
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.stagemanager.JacksonSerializeStageManager
import playwrigkt.skript.venue.AMQPVenue

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

        val sqlConnectionProvider by lazy { playwrigkt.skript.stagemanager.JDBCDataSourceStageManager(hikariDataSource) }
        val publishVenue by lazy { playwrigkt.skript.stagemanager.AMQPPublishStageManager(AMQPManager.amqpExchange, amqpConnection, AMQPManager.basicProperties) }
        val serializeVenue = JacksonSerializeStageManager()

        val provider: ApplicationStageManager by lazy {
            ApplicationStageManager(publishVenue, sqlConnectionProvider, serializeVenue)
        }

        val CONSUMER_TROUPE: QueueVenue = AMQPVenue(amqpConnection)
    }

    override fun provider(): ApplicationStageManager = provider
    override fun consumerPerformerProvider(): QueueVenue = CONSUMER_TROUPE
    override fun closeResources() {
        hikariDataSource.close()
        amqpConnection.close()
    }


}