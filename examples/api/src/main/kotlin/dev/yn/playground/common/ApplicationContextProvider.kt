package dev.yn.playground.common

import dev.yn.playground.Skript
import dev.yn.playground.context.*
import dev.yn.playground.publisher.PublishSkriptExecutor
import dev.yn.playground.result.AsyncResult
import dev.yn.playground.serialize.SerializeSkriptExecutor
import dev.yn.playground.sql.SQLExecutor

class ApplicationContextProvider(
        val publishProvider: PublishSkriptContextProvider<PublishSkriptExecutor>,
        val sqlProvider: SQLSkriptContextProvider<SQLExecutor>,
        val serializeProvider: SerializeSkriptContextProvider<SerializeSkriptExecutor>
): ContextProvider<ApplicationContext> {

    private fun getPublishExecutor(): AsyncResult<PublishSkriptExecutor> {
        return publishProvider.getPublishExecutor()
    }

    private fun getConnection(): AsyncResult<SQLExecutor> = sqlProvider.getConnection()

    override fun provideContext(): AsyncResult<ApplicationContext> {
        return getConnection()
                .flatMap { sqlExecutor ->
                    getPublishExecutor().flatMap { publishExecutor ->
                        serializeProvider.getSerializeSkriptExecutor().map { serializeExecutor ->
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

class ApplicationContext(val publishSkriptExecutor: PublishSkriptExecutor, val sqlExecutor: SQLExecutor, val serializeExecutor: SerializeSkriptExecutor):
        PublishSkriptContext<PublishSkriptExecutor>,
        SQLSkriptContext<SQLExecutor>,
        SerializeSkriptContext<SerializeSkriptExecutor>{
    override fun getPublishExecutor(): PublishSkriptExecutor = publishSkriptExecutor
    override fun getSerializeSkriptExecutor(): SerializeSkriptExecutor = serializeExecutor
    override fun getSQLExecutor(): SQLExecutor = sqlExecutor
}