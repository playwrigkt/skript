package playwrigkt.skript.troupe

import io.vertx.core.Future
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection
import playwrigkt.skript.performer.SqlPerformer
import playwrigkt.skript.performer.VertxSqlPerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.VertxResult

data class VertxSqlTroupe(val sqlClient: SQLClient): SqlTroupe {
    val performer: AsyncResult<VertxSqlPerformer> by lazy {
        val future = Future.future<SQLConnection>()
        sqlClient.getConnection(future.completer())
        VertxResult(future.map { VertxSqlPerformer(it) })
    }

    override fun getSQLPerformer(): AsyncResult<out SqlPerformer> = performer
}