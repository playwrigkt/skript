package playwrigkt.skript.stagemanager

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import playwrigkt.skript.coroutine.runAsync
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.CoroutineJDBCSQLTroupe
import playwrigkt.skript.troupe.SQLTroupe

data class CoroutineJDBCDataSourceStageManager(val hikariConfig: HikariConfig): StageManager<SQLTroupe> {
    private val dataSource by lazy { HikariDataSource(hikariConfig) }

    override fun hireTroupe(): SQLTroupe = CoroutineJDBCSQLTroupe(dataSource)

    override fun tearDown(): AsyncResult<Unit> = runAsync { dataSource.close() }
}