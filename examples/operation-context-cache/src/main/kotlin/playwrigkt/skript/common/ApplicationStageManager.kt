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
        val publishStageManager: StageManager<QueuePublishPerformer>,
        val sqlStageManager: StageManager<SQLPerformer>,
        val serializeStageManager: StageManager<SerializePerformer>
): StageManager<ApplicationTroupe<Unit>> {

    override fun hireTroupe(): AsyncResult<ApplicationTroupe<Unit>> = provideTroupe(Unit)

    fun <R> provideTroupe(r: R): AsyncResult<ApplicationTroupe<R>> {
        return sqlStageManager.hireTroupe().flatMap { sqlPerformer ->
                    publishStageManager.hireTroupe().flatMap { publishPerformer ->
                        serializeStageManager.hireTroupe().map { serializePerformer ->
                            ApplicationTroupe(publishPerformer, sqlPerformer, serializePerformer, r)
                        }
                    }
                }
    }

    fun <I, O, R> runWithTroupe(skript: Skript<I, O, ApplicationTroupe<R>>, i: I, r: R): AsyncResult<O> {
        return provideTroupe(r)
                .flatMap { skript.run(i, it) }
    }
}


interface TroupeProps<R> {
    fun getTroupeProps(): R
}

class ApplicationTroupe<R>(
        private val publishPerformer: QueuePublishPerformer,
        private val sqlPerformer: SQLPerformer,
        private val serializePerformer: SerializePerformer,
        private val cache: R):
        QueuePublishTroupe,
        SQLTroupe,
        SerializeTroupe,
        TroupeProps<R>
{
    override fun getSerializePerformer(): SerializePerformer = serializePerformer
    override fun getTroupeProps(): R = cache
    override fun getPublishPerformer(): QueuePublishPerformer = publishPerformer
    override fun getSQLPerformer(): SQLPerformer = sqlPerformer
}