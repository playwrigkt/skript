package playwrigkt.skript.stagemanager

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLClient
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.troupe.SqlTroupe
import playwrigkt.skript.troupe.VertxSqlTroupe
import playwrigkt.skript.vertx.ex.vertxHandler

data class VertxSqlStageManager(val vertx: Vertx, val sqlConfig: JsonObject, val datasourceName: String): StageManager<SqlTroupe> {
    val sqlClient: SQLClient by lazy {
        JDBCClient.createShared(vertx, sqlConfig, "test_ds")
    }

    override fun hireTroupe(): SqlTroupe = VertxSqlTroupe(sqlClient)

    override fun tearDown(): AsyncResult<Unit> {
        val result = CompletableResult<Unit>()
        sqlClient.close(result.vertxHandler())
        return result.map { Unit }
    }
}