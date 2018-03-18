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
        val publishVenue: Venue<out PublishPerformer>,
        val sqlVenue: Venue<out SQLPerformer>,
        val serializeVenue: Venue<out SerializePerformer>
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
        PublishStage,
        SQLStage,
        SerializeStage,
        StageProps<R>
{
    override fun getSerializePerformer(): SerializePerformer = serializePerformer
    override fun getStageProps(): R = cache
    override fun getPublishPerformer(): PublishPerformer = publishPerformer
    override fun getSQLPerformer(): SQLPerformer = sqlPerformer
}