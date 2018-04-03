package playwrigkt.skript.troupe

import kotlinx.coroutines.experimental.launch
import playwrigkt.skript.performer.CoroutineJDBCPerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import javax.sql.DataSource

data class JDBCSQLTroupe(val dataSource: DataSource): SQLTroupe {
    val sqlPerformer: AsyncResult<CoroutineJDBCPerformer> by lazy {
        val result = CompletableResult<CoroutineJDBCPerformer>()
        launch {
            try {
                result.succeed(CoroutineJDBCPerformer(dataSource.connection))
            } catch(e: Throwable) {
                result.fail(e)
            }
        }
        result
    }
    override fun getSQLPerformer(): AsyncResult<CoroutineJDBCPerformer> = sqlPerformer
}