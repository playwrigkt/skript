package playwrigkt.skript.stagemanager

import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.*

data class ApplicationStageManager(
        val publishProvider: StageManager<QueuePublishTroupe>,
        val sqlProvider: StageManager<SQLTroupe>,
        val serializeProvider: StageManager<SerializeTroupe>,
        val httpManager: StageManager<HttpRequestTroupe>
): StageManager<ApplicationTroupe> {
    override fun hireTroupe(): ApplicationTroupe =
            ApplicationTroupe(publishProvider.hireTroupe(), sqlProvider.hireTroupe(), serializeProvider.hireTroupe(), httpManager.hireTroupe())

    fun <I, O> runWithTroupe(skript: Skript<I, O, ApplicationTroupe>, i: I): AsyncResult<O> {
        return skript.run(i, hireTroupe())
    }
}

