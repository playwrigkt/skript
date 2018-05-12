package playwrigkt.skript.application

import com.zaxxer.hikari.HikariConfig
import playwrigkt.skript.Skript
import playwrigkt.skript.stagemanager.CoroutineJDBCDataSourceStageManager
import playwrigkt.skript.troupe.SQLTroupe
import java.util.*

class CoroutineJDBCSQLModule: SkriptModule {
    override fun loaders(): List<StageManagerLoader<*>> = listOf(CoroutineJDBCSQLStageManagerLoader)

}

object CoroutineJDBCSQLStageManagerLoader: StageManagerLoader<SQLTroupe> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "coroutine-jdbc-sql"

    override val loadManager: Skript<StageManagerLoader.Input, CoroutineJDBCDataSourceStageManager, SkriptApplicationLoader> = Skript.identity<StageManagerLoader.Input, SkriptApplicationLoader>()
                .mapTry { it.stageManagerLoaderConfig.config.applyPath("dataSource", ".") }
                .map {
                    val properties = Properties()
                    properties.putAll(it.propertiesList())
                    HikariConfig(properties)
                }
                .map { CoroutineJDBCDataSourceStageManager(it) }
}