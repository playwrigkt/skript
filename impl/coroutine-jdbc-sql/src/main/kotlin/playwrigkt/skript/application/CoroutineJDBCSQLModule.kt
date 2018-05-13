package playwrigkt.skript.application

import com.zaxxer.hikari.HikariConfig
import playwrigkt.skript.Skript
import playwrigkt.skript.stagemanager.CoroutineJDBCDataSourceStageManager
import java.util.*

class CoroutineJDBCSQLModule: SkriptModule {
    override fun loaders(): List<ApplicationResourceLoader<*>> = listOf(CoroutineJDBCSQLStageManagerLoader)

}

object CoroutineJDBCSQLStageManagerLoader: ApplicationResourceLoader<CoroutineJDBCDataSourceStageManager> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "coroutine-jdbc-sql"

    override val loadResource: Skript<ApplicationResourceLoader.Input, CoroutineJDBCDataSourceStageManager, SkriptApplicationLoader> = Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                .mapTry { it.applicationResourceLoaderConfig.config.applyPath("dataSource", ".") }
                .map {
                    val properties = Properties()
                    properties.putAll(it.propertiesList())
                    HikariConfig(properties)
                }
                .map { CoroutineJDBCDataSourceStageManager(it) }
}