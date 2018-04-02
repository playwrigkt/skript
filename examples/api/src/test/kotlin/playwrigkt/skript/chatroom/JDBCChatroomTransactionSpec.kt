package playwrigkt.skript.chatroom

import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import playwrigkt.skript.amqp.AMQPManager
import playwrigkt.skript.common.ApplicationStageManager
import playwrigkt.skript.stagemanager.JacksonSerializeStageManager
import playwrigkt.skript.user.JDBCUserServiceSpec

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
        val sqlConnectionProvider = playwrigkt.skript.stagemanager.JDBCDataSourceStageManager(hikariDataSource)
        val publishVenue by lazy { playwrigkt.skript.stagemanager.AMQPPublishStageManager(AMQPManager.amqpExchange, JDBCUserServiceSpec.amqpConnection, AMQPManager.basicProperties) }
        val serializeVenue = JacksonSerializeStageManager()
        val provider: ApplicationStageManager by lazy {
            ApplicationStageManager(publishVenue, sqlConnectionProvider, serializeVenue)
        }
    }

    override fun provider(): ApplicationStageManager = JDBCChatroomTransactionSpec.provider

    override fun closeResources() {
        hikariDataSource.close()
        amqpConnection.close()
    }
}