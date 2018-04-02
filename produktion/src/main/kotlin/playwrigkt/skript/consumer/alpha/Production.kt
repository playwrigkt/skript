package playwrigkt.skript.consumer.alpha

import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.venue.StageManager

interface Production {
    fun isRunning(): Boolean
    fun stop(): AsyncResult<Unit>
    fun result(): AsyncResult<Unit>
}

data class Performance<I, O, Troupe>(
        val skript: Skript<I, O, Troupe>,
        val stageManager: StageManager<Troupe>
) {
    fun run(i: I): AsyncResult<O> = skript.run(i, stageManager.hireTroupe())
}