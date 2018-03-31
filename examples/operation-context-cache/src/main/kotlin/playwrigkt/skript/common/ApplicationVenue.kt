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

class ApplicationVenue (
        val publishVenue: Venue<PublishPerformer>,
        val sqlVenue: Venue<SQLPerformer>,
        val serializeVenue: Venue<SerializePerformer>
): Venue<ApplicationStage<Unit>> {

    override fun provideStage(): AsyncResult<ApplicationStage<Unit>> = provideStage(Unit)

    fun <R> provideStage(r: R): AsyncResult<ApplicationStage<R>> {
        return sqlVenue.provideStage().flatMap { sqlPerformer ->
                    publishVenue.provideStage().flatMap { publishPerformer ->
                        serializeVenue.provideStage().map { serializePerformer ->
                            ApplicationStage(publishPerformer, sqlPerformer, serializePerformer, r)
                        }
                    }
                }
    }

    fun <I, O, R> runOnStage(skript: Skript<I, O, ApplicationStage<R>>, i: I, r: R): AsyncResult<O> {
        return provideStage(r)
                .flatMap { skript.run(i, it) }
    }
}


interface StageProps<R> {
    fun getStageProps(): R
}

class ApplicationStage<R>(
        private val publishPerformer: PublishPerformer,
        private val sqlPerformer: SQLPerformer,
        private val serializePerformer: SerializePerformer,
        private val cache: R):
        PublishCast,
        SQLCast,
        SerializeCast,
        StageProps<R>
{
    override fun getSerializePerformer(): SerializePerformer = serializePerformer
    override fun getStageProps(): R = cache
    override fun getPublishPerformer(): PublishPerformer = publishPerformer
    override fun getSQLPerformer(): SQLPerformer = sqlPerformer
}