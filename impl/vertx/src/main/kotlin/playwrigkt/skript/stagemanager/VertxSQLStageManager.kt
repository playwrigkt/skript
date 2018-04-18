package playwrigkt.skript.stagemanager

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLClient
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.troupe.SQLTroupe
import playwrigkt.skript.troupe.VertxSQLTroupe
import playwrigkt.skript.vertx.ex.vertxHandler

data class VertxSQLStageManager(val vertx: Vertx, val sqlConfig: JsonObject, val datasourceName: String): StageManager<SQLTroupe> {
    val sqlClient: SQLClient by lazy {
        JDBCClient.createShared(vertx, sqlConfig, "test_ds")
    }

    override fun hireTroupe(): SQLTroupe = VertxSQLTroupe(sqlClient)

    override fun tearDown(): AsyncResult<Unit> {
        val result = CompletableResult<Unit>()
        sqlClient.close(result.vertxHandler())
        return result.map { Unit }
    }
}