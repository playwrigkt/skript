package playwrigkt.skript.venue

import kotlinx.coroutines.experimental.launch
import playwrigkt.skript.performer.CoroutineJDBCPerformer
import playwrigkt.skript.performer.SQLPerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.troupe.SQLTroupe
import javax.sql.DataSource

data class JDBCDataSourceStageManager(val dataSource: DataSource): StageManager<SQLTroupe> {
    override fun hireTroupe(): SQLTroupe =
        object: SQLTroupe {
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
            override fun getSQLPerformer(): AsyncResult<CoroutineJDBCPerformer> = sqlPerformer.copy()
        }
}