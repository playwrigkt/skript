package playwrigkt.skript.application

import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.stagemanager.SyncFileStageManager
import playwrigkt.skript.troupe.FileTroupe
import playwrigkt.skript.troupe.SyncFileTroupe

object SyncFileStageManagerLoader: ApplicationResourceLoader<SyncFileStageManager> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "file"

    override val loadResource: Skript<ApplicationResourceLoader.Input, SyncFileStageManager, SkriptApplicationLoader> =
            Skript.map { SyncFileStageManager }
}