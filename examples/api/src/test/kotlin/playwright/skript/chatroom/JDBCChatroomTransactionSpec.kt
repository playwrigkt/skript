package playwright.skript.chatroom

import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import playwright.skript.amqp.AMQPManager
import playwright.skript.common.ApplicationVenue
import playwright.skript.user.JDBCUserServiceSpec
import playwright.skript.venue.AMQPPublishVenue
import playwright.skript.venue.JacksonSerializeVenue

class JDBCChatroomTransactionSpec: ChatroomTransactionsSpec() {

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

        val hikariDataSource = HikariDataSource(hikariDSConfig)
        val sqlConnectionProvider = playwright.skript.venue.JDBCDataSourceVenue(hikariDataSource)
        val publishVenue by lazy { AMQPPublishVenue(AMQPManager.amqpExchange, JDBCUserServiceSpec.amqpConnection, AMQPManager.basicProperties) }
        val serializeVenue = JacksonSerializeVenue()
        val provider: ApplicationVenue by lazy {
            ApplicationVenue(publishVenue, sqlConnectionProvider, serializeVenue)
        }
    }

    override fun provider(): ApplicationVenue = JDBCChatroomTransactionSpec.provider

    override fun closeResources() {
        hikariDataSource.close()
        amqpConnection.close()
    }
}