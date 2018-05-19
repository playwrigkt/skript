package playwrigkt.skript.application

import com.zaxxer.hikari.HikariConfig
import playwrigkt.skript.Skript
import playwrigkt.skript.stagemanager.CoroutineJdbcSqlStageManager
import java.util.*

class CoroutineJdbcSqlModule: SkriptModule {
    override fun loaders(): List<ApplicationResourceLoader<*>> = listOf(CoroutineJdbcSqlStageManagerLoader)
}

object CoroutineJdbcSqlStageManagerLoader: ApplicationResourceLoader<CoroutineJdbcSqlStageManager> {
    override val dependencies: List<String> = emptyList()

    override val loadResource: Skript<ApplicationResourceLoader.Input, CoroutineJdbcSqlStageManager, SkriptApplicationLoader> = Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                .mapTry { it.applicationResourceLoaderConfig.config.applyPath("dataSource", ".") }
                .map {
                    val properties = Properties()
                    properties.putAll(it.propertiesList())
                    HikariConfig(properties)
                }
                .map { CoroutineJdbcSqlStageManager(it) }
}