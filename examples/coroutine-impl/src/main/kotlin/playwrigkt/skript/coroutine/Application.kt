package playwrigkt.skript.coroutine

import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import playwrigkt.skript.ExampleApplication
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.stagemanager.JDBCDataSourceStageManager
import playwrigkt.skript.stagemanager.JacksonSerializeStageManager
import playwrigkt.skript.stagemanager.KtorHttpClientStageManager
import playwrigkt.skript.venue.AMQPVenue
import playwrigkt.skript.venue.KtorHttpServerVenue
import playwrigkt.skript.venue.QueueVenue

fun startApplication(amqpConnectionFactory: ConnectionFactory, hikariConfig: HikariConfig, port: Int): ExampleApplication {
    val amqpVenue: QueueVenue by lazy { AMQPVenue(amqpConnectionFactory) }
    val httpServerVenue: KtorHttpServerVenue by lazy { KtorHttpServerVenue(port, 10000) }

    val sqlConnectionStageManager = JDBCDataSourceStageManager(hikariConfig)
    val publishStageManager = playwrigkt.skript.stagemanager.AMQPPublishStageManager(AMQPManager.amqpExchange, amqpConnectionFactory, AMQPManager.basicProperties)
    val serializeStageManager = JacksonSerializeStageManager()
    val httpCientStageManager = KtorHttpClientStageManager()
    val stageManager = ApplicationStageManager(publishStageManager, sqlConnectionStageManager, serializeStageManager, httpCientStageManager)

    return ExampleApplication(stageManager, httpServerVenue, amqpVenue)
}
