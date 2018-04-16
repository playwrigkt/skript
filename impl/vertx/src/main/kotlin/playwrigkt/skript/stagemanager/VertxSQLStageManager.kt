package playwrigkt.skript.stagemanager

import io.vertx.ext.sql.SQLClient
import playwrigkt.skript.troupe.SQLTroupe
import playwrigkt.skript.troupe.VertxSQLTroupe

data class VertxSQLStageManager(val sqlClient: SQLClient): StageManager<SQLTroupe> {
    override fun hireTroupe(): SQLTroupe = VertxSQLTroupe(sqlClient)
}