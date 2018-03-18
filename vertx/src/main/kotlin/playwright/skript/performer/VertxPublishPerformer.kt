package playwright.skript.performer

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import org.funktionale.tries.Try
import playwright.skript.result.AsyncResult

class VertxPublishPerformer(val eventBus: EventBus): PublishPerformer {
    override fun publish(command: PublishCommand.Publish): AsyncResult<Unit> {
        val publishResult = Try { eventBus.publish(command.target, Buffer.buffer(command.body)) }
        return when(publishResult) {
            is Try.Success -> AsyncResult.succeeded(Unit)
            is Try.Failure -> AsyncResult.failed(publishResult.throwable)
        }
    }
}