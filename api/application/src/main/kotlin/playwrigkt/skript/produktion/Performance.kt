package playwrigkt.skript.produktion

import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager

data class Performance<I, O, Troupe>(
        val skript: Skript<I, O, Troupe>,
        val stageManager: StageManager<Troupe>
) {
    fun run(i: I): AsyncResult<O> = skript.run(i, stageManager.hireTroupe())
}