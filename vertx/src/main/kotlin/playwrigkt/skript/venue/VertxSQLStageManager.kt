package playwrigkt.skript.venue

import io.vertx.core.Future
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection
import playwrigkt.skript.performer.SQLPerformer
import playwrigkt.skript.performer.VertxSQLPerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.VertxResult
import playwrigkt.skript.troupe.SQLTroupe

data class VertxSQLStageManager(val sqlClient: SQLClient): StageManager<SQLTroupe> {
    override fun hireTroupe(): SQLTroupe =

            object: SQLTroupe {
                val performer: AsyncResult<VertxSQLPerformer> by lazy {
                    val future = Future.future<SQLConnection>()
                    sqlClient.getConnection(future.completer())
                   VertxResult(future.map { VertxSQLPerformer(it) })
                }

                override fun getSQLPerformer(): AsyncResult<out SQLPerformer> = performer.copy()
            }
}