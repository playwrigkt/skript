package playwrigkt.skript.chatroom

import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import playwrigkt.skript.amqp.AMQPManager
import playwrigkt.skript.performer.HttpClientPerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.stagemanager.JacksonSerializeStageManager
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.troupe.HttpClientTroupe
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

        val hikariDataSource by lazy { HikariDataSource(hikariDSConfig) }
        val sqlConnectionStageManager by lazy { playwrigkt.skript.stagemanager.JDBCDataSourceStageManager(hikariDataSource) }
        val publishStageManager by lazy { playwrigkt.skript.stagemanager.AMQPPublishStageManager(AMQPManager.amqpExchange, JDBCUserServiceSpec.amqpConnection, AMQPManager.basicProperties) }
        val serializeStageManager by lazy { JacksonSerializeStageManager() }
        val HTTP_CLIENT_STAGE_MANAGER: StageManager<HttpClientTroupe> by lazy {
            object: StageManager<HttpClientTroupe> {
                override fun hireTroupe(): HttpClientTroupe =
                        object: HttpClientTroupe {
                            override fun getHttpRequestPerformer(): AsyncResult<out HttpClientPerformer> =
                                    TODO("not implemented")
                        }
            }
        }
        val stageManager: ApplicationStageManager by lazy {
            ApplicationStageManager(publishStageManager, sqlConnectionStageManager, serializeStageManager, HTTP_CLIENT_STAGE_MANAGER)
        }
    }

    override fun stageManager(): ApplicationStageManager = JDBCChatroomTransactionSpec.stageManager

    override fun closeResources() {
        hikariDataSource.close()
        amqpConnection.close()
    }
}