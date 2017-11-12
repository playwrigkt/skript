package dev.yn.playground.common

import dev.yn.playground.consumer.alpha.ContextProvider
import dev.yn.playground.publisher.PublishTaskContext
import dev.yn.playground.publisher.PublishTaskContextProvider
import dev.yn.playground.publisher.PublishTaskExecutor
import dev.yn.playground.sql.context.SQLExecutor
import dev.yn.playground.sql.context.SQLTaskContextProvider
import dev.yn.playground.sql.context.SQLTaskContext
import dev.yn.playground.task.Task
import dev.yn.playground.task.result.AsyncResult
import dev.yn.playground.vertx.task.VertxTaskContext
import io.vertx.core.Vertx

class ApplicationContextProvider(
        val publishProvider: PublishTaskContextProvider<PublishTaskExecutor>,
        val sqlProvider: SQLTaskContextProvider<SQLExecutor>,
        val vertx: Vertx//TODO this should be a ConsumerContextProvider<ConsumerExecutor>
): ContextProvider<ApplicationContext> {

    private fun getPublishExecutor(): AsyncResult<PublishTaskExecutor> {
        return publishProvider.getPublishExecutor()
    }

    private fun getConnection(): AsyncResult<SQLExecutor> = sqlProvider.getConnection()

    override fun provideContext(): AsyncResult<ApplicationContext> {
        return getConnection()
                .flatMap { sqlExecutor ->
                    getPublishExecutor().map { publishExecutor ->
                        ApplicationContext(vertx, publishExecutor, sqlExecutor)
                    }
                }
    }

    fun <I, O> runOnContext(task: Task<I, O, ApplicationContext>, i: I): AsyncResult<O> {
        return provideContext()
                .flatMap { task.run(i, it) }
    }
}

class ApplicationContext(private val vertx: Vertx, val publishTaskExecutor: PublishTaskExecutor, val sqlExecutor: SQLExecutor):
        PublishTaskContext<PublishTaskExecutor>,
        SQLTaskContext<SQLExecutor>,
        VertxTaskContext {
    override fun getVertx(): Vertx = vertx
    override fun getPublishExecutor(): PublishTaskExecutor = publishTaskExecutor
    override fun getSQLActionContext(): SQLExecutor = sqlExecutor
}