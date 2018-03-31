package playwrigkt.skript.venue

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection
import org.funktionale.tries.Try
import playwrigkt.skript.publish.AMQPPublishPerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.toAsyncResult
import playwrigkt.skript.venue.Venue

class AMQPPublishVenue(
        val exchange: String,
        val connection: Connection,
        val basicProperties: AMQP.BasicProperties): Venue<playwrigkt.skript.publish.AMQPPublishPerformer> {
    override fun provideStage(): AsyncResult<playwrigkt.skript.publish.AMQPPublishPerformer> =
            Try { playwrigkt.skript.publish.AMQPPublishPerformer(exchange, connection.createChannel(), basicProperties) }
                    .toAsyncResult()
}