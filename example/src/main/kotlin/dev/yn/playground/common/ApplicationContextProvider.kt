package dev.yn.playground.common

import dev.yn.playground.consumer.alpha.CacheContextProvider
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

class ApplicationContextProvider (
        val publishProvider: PublishTaskContextProvider<PublishTaskExecutor>,
        val sqlProvider: SQLTaskContextProvider<SQLExecutor>,
        val vertx: Vertx//TODO this should be a ConsumerContextProvider<ConsumerExecutor>
): ContextProvider<ApplicationContext<Unit>> {

    override fun provideContext(): AsyncResult<ApplicationContext<Unit>> = provideContext(Unit)

    private fun getPublishExecutor(): AsyncResult<PublishTaskExecutor> {
        return publishProvider.getPublishExecutor()
    }

    private fun getConnection(): AsyncResult<SQLExecutor> = sqlProvider.getConnection()

    fun <R> provideContext(r: R): AsyncResult<ApplicationContext<R>> {
        return getConnection()
                .flatMap { sqlExecutor ->
                    getPublishExecutor().map { publishExecutor ->
                        ApplicationContext(vertx, publishExecutor, sqlExecutor, r)
                    }
                }
    }

    fun <I, O, R> runOnContext(task: Task<I, O, ApplicationContext<R>>, i: I, r: R): AsyncResult<O> {
        return provideContext(r)
                .flatMap { task.run(i, it) }
    }
}

class CacheApplicationContextProvider<R>(
        val publishProvider: PublishTaskContextProvider<PublishTaskExecutor>,
        val sqlProvider: SQLTaskContextProvider<SQLExecutor>,
        val vertx: Vertx
): CacheContextProvider<ApplicationContext<R>, R> {

    private fun getPublishExecutor(): AsyncResult<PublishTaskExecutor> {
        return publishProvider.getPublishExecutor()
    }

    private fun getConnection(): AsyncResult<SQLExecutor> = sqlProvider.getConnection()

    override fun provideContext(r: R): AsyncResult<ApplicationContext<R>> {
        return getConnection()
                .flatMap { sqlExecutor ->
                    getPublishExecutor().map { publishExecutor ->
                        ApplicationContext(vertx, publishExecutor, sqlExecutor, r)
                    }
                }
    }

}

interface OperationCache<R> {
    fun getOperationCache(): R
}

class ApplicationContext<R>(private val vertx: Vertx,
                         val publishTaskExecutor: PublishTaskExecutor,
                         val sqlExecutor: SQLExecutor,
                         val cache: R):
        PublishTaskContext<PublishTaskExecutor>,
        SQLTaskContext<SQLExecutor>,
        VertxTaskContext,
        OperationCache<R>
{
    override fun getOperationCache(): R = cache
    override fun getVertx(): Vertx = vertx
    override fun getPublishExecutor(): PublishTaskExecutor = publishTaskExecutor
    override fun getSQLExecutor(): SQLExecutor = sqlExecutor
}