package dev.yn.playground.common

import dev.yn.playground.Skript
import dev.yn.playground.context.*
import dev.yn.playground.publisher.PublishTaskExecutor
import dev.yn.playground.result.AsyncResult
import dev.yn.playground.serialize.SerializeTaskExecutor
import dev.yn.playground.sql.SQLExecutor

class ApplicationContextProvider(
        val publishProvider: PublishTaskContextProvider<PublishTaskExecutor>,
        val sqlProvider: SQLTaskContextProvider<SQLExecutor>,
        val serializeProvider: SerializeTaskContextProvider<SerializeTaskExecutor>
): ContextProvider<ApplicationContext> {

    private fun getPublishExecutor(): AsyncResult<PublishTaskExecutor> {
        return publishProvider.getPublishExecutor()
    }

    private fun getConnection(): AsyncResult<SQLExecutor> = sqlProvider.getConnection()

    override fun provideContext(): AsyncResult<ApplicationContext> {
        return getConnection()
                .flatMap { sqlExecutor ->
                    getPublishExecutor().flatMap { publishExecutor ->
                        serializeProvider.getSerializeTaskExecutor().map { serializeExecutor ->
                            ApplicationContext(publishExecutor, sqlExecutor, serializeExecutor)
                        }
                    }
                }
    }

    fun <I, O> runOnContext(skript: Skript<I, O, ApplicationContext>, i: I): AsyncResult<O> {
        return provideContext()
                .flatMap { skript.run(i, it) }
    }
}

class ApplicationContext(val publishTaskExecutor: PublishTaskExecutor, val sqlExecutor: SQLExecutor, val serializeExecutor: SerializeTaskExecutor):
        PublishTaskContext<PublishTaskExecutor>,
        SQLTaskContext<SQLExecutor>,
        SerializeTaskContext<SerializeTaskExecutor>{
    override fun getPublishExecutor(): PublishTaskExecutor = publishTaskExecutor
    override fun getSerializeTaskExecutor(): SerializeTaskExecutor = serializeExecutor
    override fun getSQLExecutor(): SQLExecutor = sqlExecutor
}