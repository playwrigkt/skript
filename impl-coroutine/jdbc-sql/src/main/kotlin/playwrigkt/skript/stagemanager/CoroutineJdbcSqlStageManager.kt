package playwrigkt.skript.stagemanager

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import playwrigkt.skript.coroutine.runAsync
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.CoroutineJdbcSqlTroupe
import playwrigkt.skript.troupe.SqlTroupe

data class CoroutineJdbcSqlStageManager(val hikariConfig: HikariConfig): StageManager<SqlTroupe> {
    private val dataSource by lazy { HikariDataSource(hikariConfig) }

    override fun hireTroupe(): SqlTroupe = CoroutineJdbcSqlTroupe(dataSource)

    override fun tearDown(): AsyncResult<Unit> = runAsync { dataSource.close() }
}