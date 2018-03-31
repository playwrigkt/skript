package playwrigkt.skript.venue

import io.vertx.core.Future
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection
import playwrigkt.skript.performer.VertxSQLPerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.VertxResult

data class VertxSQLVenue(val sqlClient: SQLClient): Venue<VertxSQLPerformer> {
    override fun provideStage(): AsyncResult<VertxSQLPerformer> {
        val future = Future.future<SQLConnection>()
        sqlClient.getConnection(future.completer())
        return VertxResult(future.map { VertxSQLPerformer(it) })
    }
}