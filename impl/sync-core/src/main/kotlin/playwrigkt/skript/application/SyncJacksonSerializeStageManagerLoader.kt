package playwrigkt.skript.application

import org.funktionale.tries.Try
import playwrigkt.skript.ex.toAsyncResult
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.stagemanager.SyncJacksonSerializeStageManager
import playwrigkt.skript.troupe.SerializeTroupe

object SyncJacksonSerializeStageManagerLoader: StageManagerLoader<SerializeTroupe> {
    override fun register(registry: ApplicationRegistry): Try<Unit> = registry.register("serialize", this, emptyList())

    override fun loadManager(existingManagers: Map<String, StageManager<*>>, config: StageManagerLoaderConfig): AsyncResult<out StageManager<SerializeTroupe>> =
            Try { SyncJacksonSerializeStageManager() }.toAsyncResult()
}