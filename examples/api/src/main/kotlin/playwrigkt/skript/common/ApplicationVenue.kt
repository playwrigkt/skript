package playwrigkt.skript.common

import playwrigkt.skript.Skript
import playwrigkt.skript.performer.PublishPerformer
import playwrigkt.skript.performer.SQLPerformer
import playwrigkt.skript.performer.SerializePerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stage.PublishCast
import playwrigkt.skript.stage.SQLCast
import playwrigkt.skript.stage.SerializeCast
import playwrigkt.skript.venue.Venue

class ApplicationVenue(
        val publishProvider: Venue<PublishPerformer>,
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
        private val publishPerformer: PublishPerformer,
        private val sqlPerformer: SQLPerformer,
        private val serializePerformer: SerializePerformer):
        PublishCast,
        SQLCast,
        SerializeCast {
    override fun getPublishPerformer(): PublishPerformer = publishPerformer
    override fun getSerializePerformer(): SerializePerformer = serializePerformer
    override fun getSQLPerformer(): SQLPerformer = sqlPerformer
}