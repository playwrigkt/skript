package playwrigkt.skript.troupe

import kotlinx.coroutines.experimental.launch
import playwrigkt.skript.performer.CoroutineJdbcSqlPerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import javax.sql.DataSource

data class CoroutineJdbcSqlTroupe(val dataSource: DataSource): SqlTroupe {
    val sqlPerformer: AsyncResult<CoroutineJdbcSqlPerformer> by lazy {
        val result = CompletableResult<CoroutineJdbcSqlPerformer>()
        launch {
            try {
                result.succeed(CoroutineJdbcSqlPerformer(dataSource.connection))
            } catch(e: Throwable) {
                result.fail(e)
            }
        }
        result
    }
    override fun getSQLPerformer(): AsyncResult<CoroutineJdbcSqlPerformer> = sqlPerformer
}