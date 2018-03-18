package playwright.skript.common

import playwright.skript.Skript
import playwright.skript.performer.PublishPerformer
import playwright.skript.performer.SQLPerformer
import playwright.skript.performer.SerializePerformer
import playwright.skript.result.AsyncResult
import playwright.skript.stage.PublishStage
import playwright.skript.stage.SQLStage
import playwright.skript.stage.SerializeStage
import playwright.skript.venue.Venue

class ApplicationVenue (
        val publishVenue: Venue<PublishPerformer>,
        val sqlVenue: Venue<SQLPerformer>,
        val serializeVenue: Venue<SerializePerformer>
): Venue<ApplicationStage<Unit>> {

    override fun provideStage(): AsyncResult<ApplicationStage<Unit>> = provideContext(Unit)

    fun <R> provideContext(r: R): AsyncResult<ApplicationStage<R>> {
        return sqlVenue.provideStage().flatMap { sqlPerformer ->
                    publishVenue.provideStage().flatMap { publishPerformer ->
                        serializeVenue.provideStage().map { serializePerformer ->
                            ApplicationStage(publishPerformer, sqlPerformer, serializePerformer, r)
                        }
                    }
                }
    }

    fun <I, O, R> runOnContext(skript: Skript<I, O, ApplicationStage<R>>, i: I, r: R): AsyncResult<O> {
        return provideContext(r)
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
        PublishStage<PublishPerformer>,
        SQLStage<SQLPerformer>,
        SerializeStage<SerializePerformer>,
        StageProps<R>
{
    override fun getSerializePerformer(): SerializePerformer = serializePerformer
    override fun getStageProps(): R = cache
    override fun getPublishPerformer(): PublishPerformer = publishPerformer
    override fun getSQLPerformer(): SQLPerformer = sqlPerformer
}