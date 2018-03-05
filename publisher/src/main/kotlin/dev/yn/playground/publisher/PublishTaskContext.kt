package dev.yn.playground.publisher

import dev.yn.playground.task.result.AsyncResult

sealed class PublishCommand {
    data class Publish(val target: String, val body: ByteArray): PublishCommand()
}
interface PublishTaskExecutor {
    fun publish(command: PublishCommand.Publish): AsyncResult<Unit>
}

interface PublishTaskContextProvider<E: PublishTaskExecutor> {
    fun getPublishExecutor(): AsyncResult<E>
}

interface PublishTaskContext<E: PublishTaskExecutor> {
    fun getPublishExecutor(): E
}