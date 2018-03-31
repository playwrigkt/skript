package playwrigkt.skript.common

import playwright.skript.performer.QueuePublishPerformer
import playwright.skript.stage.QueuePublishStage
import playwrigkt.skript.Skript
import playwrigkt.skript.performer.SQLPerformer
import playwrigkt.skript.performer.SerializePerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stage.SQLStage
import playwrigkt.skript.stage.SerializeStage
import playwrigkt.skript.venue.Venue

class ApplicationVenue(
        val publishProvider: Venue<QueuePublishPerformer>,
        val sqlProvider: Venue<SQLPerformer>,
        val serializeProvider: Venue<SerializePerformer>
): Venue<ApplicationStage> {
    override fun provideStage(): AsyncResult<ApplicationStage> {
        return sqlProvider.provideStage().flatMap { sqlPerformer ->
                    publishProvider.provideStage().flatMap { publishPerformer ->
                        serializeProvider.provideStage().map { serializePerformer ->
                            ApplicationStage(publishPerformer, sqlPerformer, serializePerformer)
                        }
                    }
                }
    }

    fun <I, O> runOnStage(skript: Skript<I, O, ApplicationStage>, i: I): AsyncResult<O> {
        return provideStage()
                .flatMap { skript.run(i, it) }
    }
}

class ApplicationStage(
        private val publishPerformer: QueuePublishPerformer,
        private val sqlPerformer: SQLPerformer,
        private val serializePerformer: SerializePerformer):
        QueuePublishStage,
        SQLStage,
        SerializeStage {
    override fun getPublishPerformer(): QueuePublishPerformer = publishPerformer
    override fun getSerializePerformer(): SerializePerformer = serializePerformer
    override fun getSQLPerformer(): SQLPerformer = sqlPerformer
}