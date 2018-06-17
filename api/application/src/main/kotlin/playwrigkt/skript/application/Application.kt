package playwrigkt.skript.application

import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.SyncJacksonSerializeStageManager
import playwrigkt.skript.troupe.SyncFileTroupe

fun createApplication(configFile: String): AsyncResult<SkriptApplication> {
    val loader = SkriptApplicationLoader(SyncFileTroupe, SyncJacksonSerializeStageManager().hireTroupe(), ApplicationRegistry())

    return loadApplication
            .run(configFile, loader)
}


