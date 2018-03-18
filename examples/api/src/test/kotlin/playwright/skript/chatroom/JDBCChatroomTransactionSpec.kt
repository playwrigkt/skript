package playwright.skript.chatroom

import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import playwright.skript.amqp.AMQPManager
import playwright.skript.common.ApplicationVenue
import playwright.skript.performer.PublishPerformer
import playwright.skript.performer.SQLPerformer
import playwright.skript.performer.SerializePerformer
import playwright.skript.user.JDBCUserServiceSpec
import playwright.skript.venue.JacksonSerializeVenue
import playwright.skript.venue.Venue

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
        val sqlConnectionProvider = playwright.skript.venue.JDBCDataSourceVenue(hikariDataSource) as Venue<SQLPerformer>
        val publishVenue by lazy { playwright.skript.stage.AMQPPublishVenue(AMQPManager.amqpExchange, JDBCUserServiceSpec.amqpConnection, AMQPManager.basicProperties) as Venue<PublishPerformer> }
        val serializeVenue = JacksonSerializeVenue() as Venue<SerializePerformer>
        val provider: ApplicationVenue by lazy {
            ApplicationVenue(publishVenue, sqlConnectionProvider, serializeVenue)
        } }

    override fun provider(): ApplicationVenue = JDBCChatroomTransactionSpec.provider

    override fun closeResources() {
        hikariDataSource.close()
        amqpConnection.close()
    }
}