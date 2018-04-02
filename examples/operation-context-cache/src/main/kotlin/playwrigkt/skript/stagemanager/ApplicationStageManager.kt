package playwrigkt.skript.stagemanager

import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.troupe.QueuePublishTroupe
import playwrigkt.skript.troupe.SQLTroupe
import playwrigkt.skript.troupe.SerializeTroupe

class ApplicationStageManager (
        val publishStageManager: StageManager<QueuePublishTroupe>,
        val sqlStageManager: StageManager<SQLTroupe>,
        val serializeStageManager: StageManager<SerializeTroupe>
): StageManager<ApplicationTroupe<Unit>> {

    override fun hireTroupe(): ApplicationTroupe<Unit> = provideTroupe(Unit)

    fun <R> provideTroupe(r: R): ApplicationTroupe<R> {
        return ApplicationTroupe(publishStageManager.hireTroupe(), sqlStageManager.hireTroupe(), serializeStageManager.hireTroupe(), r)
    }

    fun <I, O, R> runWithTroupe(skript: Skript<I, O, ApplicationTroupe<R>>, i: I, r: R): AsyncResult<O> =
            skript.run(i, provideTroupe(r))
}

