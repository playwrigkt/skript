package playwrigkt.skript.user

import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotlintest.Description
import io.kotlintest.Spec
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import playwrigkt.skript.amqp.AMQPManager
import playwrigkt.skript.chatroom.JDBCChatroomTransactionSpec
import playwrigkt.skript.produktion.Produktion
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.*
import playwrigkt.skript.venue.*
import kotlin.math.floor

class JDBCUserServiceSpec: UserServiceSpec() {
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
        val publishStageManager by lazy { AMQPPublishStageManager(AMQPManager.amqpExchange, amqpConnectionFactory, AMQPManager.basicProperties) }
        val serializeStageManagerr by lazy { JacksonSerializeStageManager() }
        val httpClientStageManager by lazy { KtorHttpClientStageManager() }
        val stageManager: ApplicationStageManager by lazy {
            ApplicationStageManager(publishStageManager, sqlConnectionStageManager, serializeStageManagerr, httpClientStageManager, httpServerVenue, amqpVenue)
        }

        val userHttpClient by lazy { UserHttpClient(port) }
    }

    override fun beforeSpec(description: Description, spec: Spec) {
        super.beforeSpec(description, spec)
        AMQPManager.cleanConnection(amqpConnectionFactory).close()
    }
    override fun afterSpec(description: Description, spec: Spec) {
        super.afterSpec(description, spec)
        AMQPManager.cleanConnection(JDBCChatroomTransactionSpec.amqpConnectionFactory).close()
    }

    override fun userHttpClient(): UserHttpClient = userHttpClient
    override fun stageManager(): ApplicationStageManager = stageManager
}