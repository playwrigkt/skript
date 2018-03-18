package playwright.skript.user

import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import playwright.skript.amqp.AMQPManager
import playwright.skript.common.ApplicationVenue
import playwright.skript.stage.AMQPPublishVenue
import playwright.skript.venue.JDBCDataSourceVenue
import playwright.skript.venue.JacksonSerializeVenue

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

        val sqlConnectionProvider by lazy { JDBCDataSourceVenue(hikariDataSource) }
        val publishVenue by lazy { AMQPPublishVenue(AMQPManager.amqpExchange, amqpConnection, AMQPManager.basicProperties) }
        val serializeVenue = JacksonSerializeVenue()

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