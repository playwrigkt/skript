package playwrigkt.skript.coroutine

import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import playwrigkt.skript.ExampleApplication
import playwrigkt.skript.ExampleApplicationStageManagerLoader
import playwrigkt.skript.application.AMQPConnectionFactoryStageManagerLoader
import playwrigkt.skript.application.ApplicationRegistry
import playwrigkt.skript.application.SkriptApplicationLoader
import playwrigkt.skript.application.loadApplication
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.*
import playwrigkt.skript.troupe.SyncFileTroupe
import playwrigkt.skript.venue.AMQPVenue
import playwrigkt.skript.venue.KtorHttpServerVenue
import playwrigkt.skript.venue.QueueVenue

fun startApplication(port: Int): AsyncResult<ExampleApplication> {
    val stageManagers = loadApplication
            .run("coroutine-application.json", SkriptApplicationLoader(SyncFileTroupe, SyncJacksonSerializeStageManager().hireTroupe(), ApplicationRegistry()))
            .map { it.stageManagers }

    val applicationStageManagerResult = stageManagers
            .map { it.get(ExampleApplicationStageManagerLoader.name) }
            .map { it as ApplicationStageManager }

    val amqpConnectionFactoryResult = stageManagers
            .map { it.get(AMQPConnectionFactoryStageManagerLoader.name) }
            .map { it as AMQPConnectionFactoryStageManager }
            .map { it.hireTroupe() }

    return applicationStageManagerResult.flatMap {applicationStageManager ->
        amqpConnectionFactoryResult.map { amqpConnectionFactory ->
            ExampleApplication(
                    applicationStageManager,
                    KtorHttpServerVenue(port, 10000),
                    AMQPVenue(amqpConnectionFactory))
        }
    }
}
