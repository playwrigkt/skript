package playwright.skript.stage

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection
import org.funktionale.tries.Try
import playwright.skript.publish.AMQPPublishPerformer
import playwright.skript.result.AsyncResult
import playwright.skript.result.toAsyncResult
import playwright.skript.venue.Venue

class AMQPPublishVenue(
        val exchange: String,
        val connection: Connection,
        val basicProperties: AMQP.BasicProperties): Venue<AMQPPublishPerformer> {
    override fun provideStage(): AsyncResult<AMQPPublishPerformer> =
            Try { AMQPPublishPerformer(exchange, connection.createChannel(), basicProperties) }
                    .toAsyncResult()
}