package playwrigkt.skript.application

import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.toAsyncResult
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.stagemanager.SyncJacksonSerializeStageManager
import playwrigkt.skript.troupe.SerializeTroupe

object SyncJacksonSerializeStageManagerLoader: StageManagerLoader<SerializeTroupe> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "serialize"

    override val loadManager: Skript<StageManagerLoader.Input, out StageManager<SerializeTroupe>, SkriptApplicationLoader> =
            Skript.map { SyncJacksonSerializeStageManager() }
}