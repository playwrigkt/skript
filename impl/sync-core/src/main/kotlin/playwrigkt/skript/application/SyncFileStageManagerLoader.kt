package playwrigkt.skript.application

import playwrigkt.skript.Skript
import playwrigkt.skript.stagemanager.SyncFileStageManager

object SyncFileStageManagerLoader: ApplicationResourceLoader<SyncFileStageManager> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "file"

    override val loadResource: Skript<ApplicationResourceLoader.Input, SyncFileStageManager, SkriptApplicationLoader> =
            Skript.map { SyncFileStageManager }
}