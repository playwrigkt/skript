package playwrigkt.skript.coroutine

import com.rabbitmq.client.ConnectionFactory
import playwrigkt.skript.ExampleApplication
import playwrigkt.skript.ExampleApplicationStageManagerLoader
import playwrigkt.skript.application.AMQPConnectionFactoryLoader
import playwrigkt.skript.application.ApplicationRegistry
import playwrigkt.skript.application.SkriptApplicationLoader
import playwrigkt.skript.application.loadApplication
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.*
import playwrigkt.skript.troupe.SyncFileTroupe
import playwrigkt.skript.venue.AMQPVenue
import playwrigkt.skript.venue.KtorHttpServerVenue

fun startApplication(port: Int): AsyncResult<ExampleApplication> {
    val stageManagers = loadApplication
            .run("coroutine-application.json", SkriptApplicationLoader(SyncFileTroupe, SyncJacksonSerializeStageManager().hireTroupe(), ApplicationRegistry()))
            .map { it.applicationResources }

    val applicationStageManagerResult = stageManagers
            .map { it.get(ExampleApplicationStageManagerLoader.name) }
            .map { it as ApplicationStageManager }

    val amqpConnectionFactoryResult = stageManagers
            .map { it.get(AMQPConnectionFactoryLoader.name) }
            .map { it as ConnectionFactory }

    return applicationStageManagerResult.flatMap {applicationStageManager ->
        amqpConnectionFactoryResult.map { amqpConnectionFactory ->
            ExampleApplication(
                    applicationStageManager,
                    KtorHttpServerVenue(port, 10000),
                    AMQPVenue(amqpConnectionFactory))
        }
    }
}
