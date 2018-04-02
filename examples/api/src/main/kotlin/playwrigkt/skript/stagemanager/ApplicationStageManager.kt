package playwrigkt.skript.stagemanager

import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.troupe.QueuePublishTroupe
import playwrigkt.skript.troupe.SQLTroupe
import playwrigkt.skript.troupe.SerializeTroupe

data class ApplicationStageManager(
        val publishProvider: StageManager<QueuePublishTroupe>,
        val sqlProvider: StageManager<SQLTroupe>,
        val serializeProvider: StageManager<SerializeTroupe>
): StageManager<ApplicationTroupe> {
    override fun hireTroupe(): ApplicationTroupe =
            ApplicationTroupe(publishProvider.hireTroupe(), sqlProvider.hireTroupe(), serializeProvider.hireTroupe())

    fun <I, O> runWithTroupe(skript: Skript<I, O, ApplicationTroupe>, i: I): AsyncResult<O> {
        val troupe = hireTroupe()
        println("troupe: $troupe")
        return skript.run(i, troupe)
    }
}

