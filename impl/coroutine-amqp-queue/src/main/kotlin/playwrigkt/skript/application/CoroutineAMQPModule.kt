package playwrigkt.skript.application

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.ConnectionFactoryConfigurator
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.all
import playwrigkt.skript.ex.deserialize
import playwrigkt.skript.ex.join
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.AMQPConnectionFactoryStageManager
import playwrigkt.skript.stagemanager.AMQPPublishStageManager
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.troupe.QueuePublishTroupe

class CoroutineAMQPModule: SkriptModule {
    override fun loaders(): List<StageManagerLoader<*>> =
            listOf(
                    CoroutineAMQPPublishStageManagerLoader,
                    AMQPConnectionFactoryStageManagerLoader)
}

object AMQPConnectionFactoryStageManagerLoader: StageManagerLoader<ConnectionFactory> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "amqp-connection-factory"
    override val loadManager: Skript<StageManagerLoader.Input, out StageManager<ConnectionFactory>, SkriptApplicationLoader> =
            Skript.identity<StageManagerLoader.Input, SkriptApplicationLoader>()
                    .mapTry { it.stageManagerLoaderConfig.config.applyPath("connection", ".") }
                    .map { it.propertiesList() }
                    .map {
                        val factory = ConnectionFactory()
                        ConnectionFactoryConfigurator.load(factory, it.toMap(), "")
                        factory
                    }
                    .map { AMQPConnectionFactoryStageManager(it) }

}
object CoroutineAMQPPublishStageManagerLoader: StageManagerLoader<QueuePublishTroupe> {
    override val dependencies: List<String> = listOf("amqp-connection-factory")
    override val name: String = "coroutine-amqp-publish"

    override val loadManager: Skript<StageManagerLoader.Input, AMQPPublishStageManager, SkriptApplicationLoader> =
            Skript.identity<StageManagerLoader.Input, SkriptApplicationLoader>()
                    .all(
                            Skript.identity<StageManagerLoader.Input, SkriptApplicationLoader>()
                                    .mapTry { it.stageManagerLoaderConfig.config.applyPath("exchange", ".") }
                                    .mapTry { it.text() }
                                    .map { it.value },
                            loadExistingStageManagerSkript<ConnectionFactory>("amqp-connection-factory")
                                    .map { it.hireTroupe() },
                            Skript.identity<StageManagerLoader.Input, SkriptApplicationLoader>()
                                    .map { AMQP.BasicProperties() }
                    ).join { exchange, connectionFactory, basicProperties ->
                        AMQPPublishStageManager(exchange, connectionFactory, basicProperties)
                    }
}

