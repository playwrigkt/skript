package playwrigkt.skript.performer

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import org.funktionale.tries.Try
import playwright.skript.performer.QueuePublishPerformer
import playwright.skript.queue.QueueMessage
import playwrigkt.skript.result.AsyncResult

class VertxPublishPerformer(val eventBus: EventBus): QueuePublishPerformer {
    override fun publish(command: QueueMessage): AsyncResult<Unit> {
        val publishResult = Try { eventBus.publish(command.source, Buffer.buffer(command.body)) }
        return when(publishResult) {
            is Try.Success -> AsyncResult.succeeded(Unit)
            is Try.Failure -> AsyncResult.failed(publishResult.throwable)
        }
    }
}