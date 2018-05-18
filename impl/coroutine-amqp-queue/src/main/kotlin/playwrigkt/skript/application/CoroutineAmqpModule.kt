package playwrigkt.skript.application

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.ConnectionFactoryConfigurator
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.all
import playwrigkt.skript.ex.join
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.AmqpPublishStageManager
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.venue.AmqpVenue

class CoroutineAmqpModule: SkriptModule {
    override fun loaders(): List<ApplicationResourceLoader<*>> =
            listOf(
                    AmqpConnectionFactoryLoader,
                    CoroutineAmqpPublishStageManagerLoader,
                    CoroutineAmqpVenueLoader)
}

object AmqpConnectionFactoryLoader: ApplicationResourceLoader<StageManager<ConnectionFactory>> {
    override val dependencies: List<String> = emptyList()
    override val loadResource: Skript<ApplicationResourceLoader.Input, StageManager<ConnectionFactory>, SkriptApplicationLoader> =
            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                    .mapTry { it.applicationResourceLoaderConfig.config.applyPath("connection", ".") }
                    .map { it.propertiesList() }
                    .map {
                        object : StageManager<ConnectionFactory> {
                            val factory by lazy {
                                val connectionFactory = ConnectionFactory()
                                ConnectionFactoryConfigurator.load(connectionFactory, it.toMap(), "")
                                connectionFactory
                            }

                            override fun hireTroupe(): ConnectionFactory = factory

                            override fun tearDown(): AsyncResult<Unit> = AsyncResult.succeeded(Unit)
                        }
                    }

}
object CoroutineAmqpPublishStageManagerLoader: ApplicationResourceLoader<AmqpPublishStageManager> {
    override val dependencies: List<String> = listOf(AmqpConnectionFactoryLoader.name())

    override val loadResource: Skript<ApplicationResourceLoader.Input, AmqpPublishStageManager, SkriptApplicationLoader> =
            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                    .all(
                            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                                    .mapTry { it.applicationResourceLoaderConfig.config.applyPath("exchange", ".") }
                                    .mapTry { it.text() }
                                    .map { it.value },
                            loadExistingApplicationResourceSkript<StageManager<ConnectionFactory>>(AmqpConnectionFactoryLoader.name())
                                    .map { it.hireTroupe() },
                            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                                    .map { AMQP.BasicProperties() }
                    ).join { exchange, connectionFactory, basicProperties ->
                        AmqpPublishStageManager(exchange, connectionFactory, basicProperties)
                    }
}

object CoroutineAmqpVenueLoader: ApplicationResourceLoader<AmqpVenue> {
    override val dependencies: List<String> = listOf(AmqpConnectionFactoryLoader.name())
    override val loadResource: Skript<ApplicationResourceLoader.Input, AmqpVenue, SkriptApplicationLoader>  =
            loadExistingApplicationResourceSkript<StageManager<ConnectionFactory>>(AmqpConnectionFactoryLoader.name())
                    .map { it.hireTroupe() }
                    .map { AmqpVenue(it) }
}

