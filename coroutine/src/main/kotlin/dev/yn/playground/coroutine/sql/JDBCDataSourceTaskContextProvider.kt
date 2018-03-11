package dev.yn.playground.coroutine.sql

import dev.yn.playground.sql.context.SQLTaskContextProvider
import dev.yn.playground.task.result.AsyncResult
import dev.yn.playground.task.result.CompletableResult
import kotlinx.coroutines.experimental.launch
import javax.sql.DataSource

data class JDBCDataSourceTaskContextProvider(val dataSource: DataSource): SQLTaskContextProvider<CoroutineJDBCExecutor> {
    override fun getConnection(): AsyncResult<CoroutineJDBCExecutor> {
        val result = CompletableResult<CoroutineJDBCExecutor>()
        launch {
            try {
                result.succeed(CoroutineJDBCExecutor(dataSource.connection))
            } catch(e: Throwable) {
                result.fail(e)
            }
        }

        return result
    }

}