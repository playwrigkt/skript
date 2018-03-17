package dev.yn.playground.common

import dev.yn.playground.Skript
import dev.yn.playground.context.*
import dev.yn.playground.publisher.PublishSkriptExecutor
import dev.yn.playground.result.AsyncResult
import dev.yn.playground.serialize.SerializeSkriptExecutor
import dev.yn.playground.sql.SQLExecutor

class ApplicationContextProvider (
        val publishProvider: PublishSkriptContextProvider<PublishSkriptExecutor>,
        val sqlProvider: SQLSkriptContextProvider<SQLExecutor>,
        val serializeProvider: SerializeSkriptContextProvider<SerializeSkriptExecutor>
): ContextProvider<ApplicationContext<Unit>> {

    override fun provideContext(): AsyncResult<ApplicationContext<Unit>> = provideContext(Unit)

    private fun getPublishExecutor(): AsyncResult<PublishSkriptExecutor> {
        return publishProvider.getPublishExecutor()
    }

    private fun getConnection(): AsyncResult<SQLExecutor> = sqlProvider.getConnection()

    private fun getSerializer(): AsyncResult<SerializeSkriptExecutor> = serializeProvider.getSerializeSkriptExecutor()

    fun <R> provideContext(r: R): AsyncResult<ApplicationContext<R>> {
        return getConnection()
                .flatMap { sqlExecutor ->
                    getPublishExecutor().flatMap { publishExecutor ->
                        getSerializer().map { serializeSkriptExecutor ->
                            ApplicationContext(publishExecutor, sqlExecutor, serializeSkriptExecutor, r)
                        }
                    }
                }
    }

    fun <I, O, R> runOnContext(skript: Skript<I, O, ApplicationContext<R>>, i: I, r: R): AsyncResult<O> {
        return provideContext(r)
                .flatMap { skript.run(i, it) }
    }
}

class CacheApplicationContextProvider<R>(
        val publishProvider: PublishSkriptContextProvider<PublishSkriptExecutor>,
        val sqlProvider: SQLSkriptContextProvider<SQLExecutor>,
        val serializeProvider: SerializeSkriptContextProvider<SerializeSkriptExecutor>
): CacheContextProvider<ApplicationContext<R>, R> {

    private fun getPublishExecutor(): AsyncResult<PublishSkriptExecutor> {
        return publishProvider.getPublishExecutor()
    }

    private fun getConnection(): AsyncResult<SQLExecutor> = sqlProvider.getConnection()

    private fun getSerializer(): AsyncResult<SerializeSkriptExecutor> = serializeProvider.getSerializeSkriptExecutor()

    override fun provideContext(r: R): AsyncResult<ApplicationContext<R>> {
        return getConnection()
                .flatMap { sqlExecutor ->
                    getPublishExecutor().flatMap { publishExecutor ->
                        getSerializer().map { serializeSkriptExecutor ->
                            ApplicationContext(publishExecutor, sqlExecutor, serializeSkriptExecutor, r)
                        }
                    }
                }
    }

}

interface OperationCache<R> {
    fun getOperationCache(): R
}

class ApplicationContext<R>(
        val publishSkriptExecutor: PublishSkriptExecutor,
        val sqlExecutor: SQLExecutor,
        val serializeExecutor: SerializeSkriptExecutor,
        val cache: R):
        PublishSkriptContext<PublishSkriptExecutor>,
        SQLSkriptContext<SQLExecutor>,
        SerializeSkriptContext<SerializeSkriptExecutor>,
        OperationCache<R>
{
    override fun getSerializeSkriptExecutor(): SerializeSkriptExecutor = serializeExecutor
    override fun getOperationCache(): R = cache
    override fun getPublishExecutor(): PublishSkriptExecutor = publishSkriptExecutor
    override fun getSQLExecutor(): SQLExecutor = sqlExecutor
}