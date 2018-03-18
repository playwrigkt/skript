package playwright.skript.venue

import kotlinx.coroutines.experimental.launch
import playwright.skript.performer.CoroutineJDBCPerformer
import playwright.skript.result.AsyncResult
import playwright.skript.result.CompletableResult
import javax.sql.DataSource

data class JDBCDataSourceVenue(val dataSource: DataSource): Venue<CoroutineJDBCPerformer> {
    override fun provideStage(): AsyncResult<CoroutineJDBCPerformer> {
        val result = CompletableResult<CoroutineJDBCPerformer>()
        launch {
            try {
                result.succeed(CoroutineJDBCPerformer(dataSource.connection))
            } catch(e: Throwable) {
                result.fail(e)
            }
        }

        return result
    }

}