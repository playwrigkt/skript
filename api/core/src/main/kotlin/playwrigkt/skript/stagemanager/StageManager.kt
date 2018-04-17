package playwrigkt.skript.stagemanager

import playwrigkt.skript.result.AsyncResult

interface StageManager<out Troupe> {
    fun hireTroupe(): Troupe

    fun tearDown(): AsyncResult<Unit>
}