package playwrigkt.skript.user

import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import playwrigkt.skript.amqp.AMQPManager
import playwrigkt.skript.performer.HttpRequestPerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.*
import playwrigkt.skript.troupe.HttpRequestTroupe
import playwrigkt.skript.venue.AMQPVenue
import playwrigkt.skript.venue.QueueVenue

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

        val sqlConnectionStageManager by lazy { JDBCDataSourceStageManager(hikariDataSource) }
        val publishStageManager by lazy { AMQPPublishStageManager(AMQPManager.amqpExchange, amqpConnection, AMQPManager.basicProperties) }
        val serializeStageManagerr by lazy { JacksonSerializeStageManager() }
        val httpRequestStageManager: StageManager<HttpRequestTroupe> by lazy {
            object: StageManager<HttpRequestTroupe> {
                override fun hireTroupe(): HttpRequestTroupe =
                    object: HttpRequestTroupe {
                        override fun getHttpRequestPerformer(): AsyncResult<out HttpRequestPerformer> =
                            TODO("not implemented")
                    }
            }
        }
        val stageManager: ApplicationStageManager by lazy {
            ApplicationStageManager(publishStageManager, sqlConnectionStageManager, serializeStageManagerr, httpRequestStageManager)
        }

        val amqpVenue: QueueVenue by lazy { AMQPVenue(amqpConnection) }
    }

    override fun stageManager(): ApplicationStageManager = stageManager
    override fun queueVenue(): QueueVenue = amqpVenue
    override fun closeResources() {
        hikariDataSource.close()
        amqpConnection.close()
    }


}