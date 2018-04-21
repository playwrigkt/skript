package playwrigkt.skript.chatroom

import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import io.kotlintest.Description
import io.kotlintest.Spec
import playwrigkt.skript.ExampleApplication
import playwrigkt.skript.amqp.AMQPManager
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.stagemanager.JDBCDataSourceStageManager
import playwrigkt.skript.stagemanager.JacksonSerializeStageManager
import playwrigkt.skript.stagemanager.KtorHttpClientStageManager
import playwrigkt.skript.venue.AMQPVenue
import playwrigkt.skript.venue.KtorHttpServerVenue
import playwrigkt.skript.venue.QueueVenue
import kotlin.math.floor

class JDBCChatroomTransactionSpec: ChatroomTransactionsSpec() {

    companion object {
        val amqpConnectionFactory: ConnectionFactory by lazy {
            AMQPManager.connectionFactory()
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

        val port = floor((Math.random() * 8000)).toInt() + 2000

        val amqpVenue: QueueVenue by lazy { AMQPVenue(amqpConnectionFactory) }
        val httpServerVenue: KtorHttpServerVenue by lazy { KtorHttpServerVenue(port, 10000) }

        val sqlConnectionStageManager by lazy { JDBCDataSourceStageManager(hikariDSConfig) }
        val publishStageManager by lazy { playwrigkt.skript.stagemanager.AMQPPublishStageManager(AMQPManager.amqpExchange, amqpConnectionFactory, AMQPManager.basicProperties) }
        val serializeStageManager by lazy { JacksonSerializeStageManager() }
        val httpCientStageManager by lazy { KtorHttpClientStageManager() }
        val stageManager: ApplicationStageManager by lazy {
            ApplicationStageManager(publishStageManager, sqlConnectionStageManager, serializeStageManager, httpCientStageManager)
        }

        val application by lazy { ExampleApplication(stageManager, httpServerVenue, amqpVenue) }
    }

    override fun beforeSpec(description: Description, spec: Spec) {
        super.beforeSpec(description, spec)
        AMQPManager.cleanConnection(amqpConnectionFactory).close()
    }

    override fun afterSpec(description: Description, spec: Spec) {
        super.afterSpec(description, spec)
        AMQPManager.cleanConnection(amqpConnectionFactory).close()
    }

    override fun application(): ExampleApplication = application
}