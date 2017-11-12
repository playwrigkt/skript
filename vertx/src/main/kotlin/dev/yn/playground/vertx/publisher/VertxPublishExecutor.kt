package dev.yn.playground.vertx.publisher

import dev.yn.playground.publisher.PublishCommand
import dev.yn.playground.publisher.PublishTaskExecutor
import dev.yn.playground.task.result.AsyncResult
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import org.funktionale.tries.Try

class VertxPublishExecutor(val eventBus: EventBus): PublishTaskExecutor {
    override fun publish(command: PublishCommand.Publish): AsyncResult<Unit> {
        val publishResult = Try { eventBus.publish(command.target, Buffer.buffer(command.body)) }
        return when(publishResult) {
            is Try.Success -> AsyncResult.succeeded(Unit)
            is Try.Failure -> AsyncResult.failed(publishResult.throwable)
        }
    }
}