package playwrigkt.skript.stagemanager

import playwrigkt.skript.troupe.JDBCSQLTroupe
import playwrigkt.skript.troupe.SQLTroupe
import javax.sql.DataSource

data class JDBCDataSourceStageManager(val dataSource: DataSource): StageManager<SQLTroupe> {
    override fun hireTroupe(): SQLTroupe = JDBCSQLTroupe(dataSource)
}