package playwrigkt.skript.common

import playwright.skript.performer.QueuePublishPerformer
import playwright.skript.troupe.QueuePublishTroupe
import playwrigkt.skript.Skript
import playwrigkt.skript.performer.SQLPerformer
import playwrigkt.skript.performer.SerializePerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.SQLTroupe
import playwrigkt.skript.troupe.SerializeTroupe
import playwrigkt.skript.venue.StageManager

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


interface TroupeProps<R> {
    fun getTroupeProps(): R
}

class ApplicationTroupe<R>(
        private val publishTroupe: QueuePublishTroupe,
        private val sqlTroupe: SQLTroupe,
        private val serializeTroupe: SerializeTroupe,
        private val cache: R):
        QueuePublishTroupe,
        SQLTroupe,
        SerializeTroupe,
        TroupeProps<R>
{
    override fun getSerializePerformer(): AsyncResult<out SerializePerformer> = serializeTroupe.getSerializePerformer()
    override fun getTroupeProps(): R = cache
    override fun getPublishPerformer(): AsyncResult<out QueuePublishPerformer> = publishTroupe.getPublishPerformer()
    override fun getSQLPerformer(): AsyncResult<out SQLPerformer> = sqlTroupe.getSQLPerformer()
}