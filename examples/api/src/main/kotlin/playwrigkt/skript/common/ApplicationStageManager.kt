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

class ApplicationStageManager(
        val publishProvider: StageManager<QueuePublishPerformer>,
        val sqlProvider: StageManager<SQLPerformer>,
        val serializeProvider: StageManager<SerializePerformer>
): StageManager<ApplicationTroupe> {
    override fun hireTroupe(): AsyncResult<ApplicationTroupe> {
        return sqlProvider.hireTroupe().flatMap { sqlPerformer ->
                    publishProvider.hireTroupe().flatMap { publishPerformer ->
                        serializeProvider.hireTroupe().map { serializePerformer ->
                            ApplicationTroupe(publishPerformer, sqlPerformer, serializePerformer)
                        }
                    }
                }
    }

    fun <I, O> runWithTroupe(skript: Skript<I, O, ApplicationTroupe>, i: I): AsyncResult<O> {
        return hireTroupe()
                .flatMap { skript.run(i, it) }
    }
}

class ApplicationTroupe(
        private val publishPerformer: QueuePublishPerformer,
        private val sqlPerformer: SQLPerformer,
        private val serializePerformer: SerializePerformer):
        QueuePublishTroupe,
        SQLTroupe,
        SerializeTroupe {
    override fun getPublishPerformer(): QueuePublishPerformer = publishPerformer
    override fun getSerializePerformer(): SerializePerformer = serializePerformer
    override fun getSQLPerformer(): SQLPerformer = sqlPerformer
}