package playwrigkt.skript.application

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.ConnectionFactoryConfigurator
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.all
import playwrigkt.skript.ex.join
import playwrigkt.skript.stagemanager.AMQPPublishStageManager

class CoroutineAMQPModule: SkriptModule {
    override fun loaders(): List<ApplicationResourceLoader<*>> =
            listOf(
                    CoroutineAMQPPublishStageManagerLoader,
                    AMQPConnectionFactoryLoader)
}

object AMQPConnectionFactoryLoader: ApplicationResourceLoader<ConnectionFactory> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "amqp-connection-factory"
    override val loadResource: Skript<ApplicationResourceLoader.Input, ConnectionFactory, SkriptApplicationLoader> =
            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                    .mapTry { it.applicationResourceLoaderConfig.config.applyPath("connection", ".") }
                    .map { it.propertiesList() }
                    .map {
                        val factory = ConnectionFactory()
                        ConnectionFactoryConfigurator.load(factory, it.toMap(), "")
                        factory
                    }

}
object CoroutineAMQPPublishStageManagerLoader: ApplicationResourceLoader<AMQPPublishStageManager> {
    override val dependencies: List<String> = listOf("amqp-connection-factory")
    override val name: String = "coroutine-amqp-publish"

    override val loadResource: Skript<ApplicationResourceLoader.Input, AMQPPublishStageManager, SkriptApplicationLoader> =
            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                    .all(
                            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                                    .mapTry { it.applicationResourceLoaderConfig.config.applyPath("exchange", ".") }
                                    .mapTry { it.text() }
                                    .map { it.value },
                            loadExistingApplicationResourceSkript<ConnectionFactory>("amqp-connection-factory"),
                            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                                    .map { AMQP.BasicProperties() }
                    ).join { exchange, connectionFactory, basicProperties ->
                        AMQPPublishStageManager(exchange, connectionFactory, basicProperties)
                    }
}

