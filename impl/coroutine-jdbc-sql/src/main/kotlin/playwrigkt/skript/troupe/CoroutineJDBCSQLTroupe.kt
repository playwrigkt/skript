package playwrigkt.skript.troupe

import kotlinx.coroutines.experimental.launch
import playwrigkt.skript.performer.CoroutineJDBCSQLPerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import javax.sql.DataSource

data class CoroutineJDBCSQLTroupe(val dataSource: DataSource): SQLTroupe {
    val sqlPerformer: AsyncResult<CoroutineJDBCSQLPerformer> by lazy {
        val result = CompletableResult<CoroutineJDBCSQLPerformer>()
        launch {
            try {
                result.succeed(CoroutineJDBCSQLPerformer(dataSource.connection))
            } catch(e: Throwable) {
                result.fail(e)
            }
        }
        result
    }
    override fun getSQLPerformer(): AsyncResult<CoroutineJDBCSQLPerformer> = sqlPerformer
}