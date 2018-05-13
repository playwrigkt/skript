package playwrigkt.skript.stagemanager

import com.fasterxml.jackson.databind.ObjectMapper
import playwrigkt.skript.jackson.SkriptJacksonObjectMapper
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.SyncJacksonSerializeTroupe
import playwrigkt.skript.troupe.SerializeTroupe

data class SyncJacksonSerializeStageManager(val objectMapper: ObjectMapper = defaultObjectMapper): StageManager<SerializeTroupe> {
    companion object {
        val defaultObjectMapper by lazy {
            SkriptJacksonObjectMapper.instance()
        }
    }

    override fun hireTroupe(): SerializeTroupe =
            SyncJacksonSerializeTroupe(objectMapper)

    override fun tearDown(): AsyncResult<Unit> {
        return AsyncResult.succeeded(Unit)
    }
}

