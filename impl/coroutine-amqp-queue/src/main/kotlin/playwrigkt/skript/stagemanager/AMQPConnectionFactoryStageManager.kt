package playwrigkt.skript.stagemanager

import com.rabbitmq.client.ConnectionFactory
import playwrigkt.skript.result.AsyncResult

data class AMQPConnectionFactoryStageManager(val connectionFactory: ConnectionFactory): StageManager<ConnectionFactory> {
    override fun hireTroupe(): ConnectionFactory = connectionFactory

    override fun tearDown(): AsyncResult<Unit> = AsyncResult.succeeded(Unit)

}