package dev.yn.playground.coroutine.sql

import dev.yn.playground.sql.context.SQLTaskContextProvider
import dev.yn.playground.task.result.AsyncResult
import dev.yn.playground.task.result.CompletableResult
import kotlinx.coroutines.experimental.launch
import javax.sql.DataSource

data class JDBCDataSourceTaskContextProvider(val dataSource: DataSource): SQLTaskContextProvider<CoroutineJDBActionContext> {
    override fun getConnection(): AsyncResult<CoroutineJDBActionContext> {
        val result = CompletableResult<CoroutineJDBActionContext>()
        launch {
            try {
                result.succeed(CoroutineJDBActionContext(dataSource.connection))
            } catch(e: Throwable) {
                result.fail(e)
            }
        }

        return result
    }

}