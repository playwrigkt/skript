package playwright.skript.venue

import io.vertx.core.Future
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection
import playwright.skript.performer.VertxSQLPerformer
import playwright.skript.result.AsyncResult
import playwright.skript.result.VertxResult

data class VertxSQLVenue(val sqlClient: SQLClient): Venue<VertxSQLPerformer> {
    override fun provideStage(): AsyncResult<VertxSQLPerformer> {
        val future = Future.future<SQLConnection>()
        sqlClient.getConnection(future.completer())
        return VertxResult(future.map { VertxSQLPerformer(it) })
    }
}