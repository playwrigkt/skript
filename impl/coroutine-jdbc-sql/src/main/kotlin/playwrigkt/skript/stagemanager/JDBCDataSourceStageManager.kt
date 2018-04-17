package playwrigkt.skript.stagemanager

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import playwrigkt.skript.coroutine.runAsync
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.JDBCSQLTroupe
import playwrigkt.skript.troupe.SQLTroupe
import javax.sql.DataSource

data class JDBCDataSourceStageManager(val hikariConfig: HikariConfig): StageManager<SQLTroupe> {
    private val dataSource by lazy { HikariDataSource(hikariConfig) }

    override fun hireTroupe(): SQLTroupe = JDBCSQLTroupe(dataSource)

    override fun tearDown(): AsyncResult<Unit> = runAsync { dataSource.close() }
}