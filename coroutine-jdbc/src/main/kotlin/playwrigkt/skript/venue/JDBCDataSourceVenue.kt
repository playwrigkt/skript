package playwrigkt.skript.venue

import kotlinx.coroutines.experimental.launch
import playwrigkt.skript.performer.CoroutineJDBCPerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import javax.sql.DataSource

data class JDBCDataSourceVenue(val dataSource: DataSource): playwrigkt.skript.venue.Venue<playwrigkt.skript.performer.CoroutineJDBCPerformer> {
    override fun provideStage(): AsyncResult<playwrigkt.skript.performer.CoroutineJDBCPerformer> {
        val result = CompletableResult<playwrigkt.skript.performer.CoroutineJDBCPerformer>()
        launch {
            try {
                result.succeed(playwrigkt.skript.performer.CoroutineJDBCPerformer(dataSource.connection))
            } catch(e: Throwable) {
                result.fail(e)
            }
        }

        return result
    }

}