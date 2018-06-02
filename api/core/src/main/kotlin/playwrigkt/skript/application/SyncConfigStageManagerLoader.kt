package playwrigkt.skript.application

import playwrigkt.skript.Skript
import playwrigkt.skript.stagemanager.SyncConfigStageManager

object SyncConfigStageManagerLoader: ApplicationResourceLoader<SyncConfigStageManager> {
    override val dependencies: List<String> = emptyList()
    override val loadResource: Skript<ApplicationResourceLoader.Input, SyncConfigStageManager, SkriptApplicationLoader> =
            Skript.map { SyncConfigStageManager(it.applicationResourceLoaderConfig.config) }

}