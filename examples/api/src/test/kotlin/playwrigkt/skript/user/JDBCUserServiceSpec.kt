package playwrigkt.skript.user

import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import playwrigkt.skript.amqp.AMQPManager
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
        val httpClient = HttpClient(Apache)
        val httpClientStageManager by lazy { KtorHttpClientStageManager(httpClient) }

        val stageManager: ApplicationStageManager by lazy {
            ApplicationStageManager(publishStageManager, sqlConnectionStageManager, serializeStageManagerr, httpClientStageManager)
        }

        val port = floor((Math.random() * 8000)).toInt() + 2000

        val amqpVenue: QueueVenue by lazy { AMQPVenue(amqpConnection) }
        val httpServerVenue: KtorHttpServerVenue by lazy { KtorHttpServerVenue(port, 10000) }
        val produktions by lazy {
            userProduktions(httpServerVenue, stageManager)
        }
        val userHttpClient by lazy { UserHttpClient(port) }
    }

    override fun produktions(): AsyncResult<List<Produktion>> = produktions
    override fun userHttpClient(): UserHttpClient = userHttpClient

    override fun stageManager(): ApplicationStageManager = stageManager
    override fun queueVenue(): QueueVenue = amqpVenue
    override fun closeResources() {
        hikariDataSource.close()
        amqpConnection.close()
        httpClient.close()
        awaitSucceededFuture(httpServerVenue.stop())
    }


}