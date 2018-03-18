package playwright.skript.stage

import playwright.skript.performer.PublishPerformer

interface PublishStage<E: PublishPerformer> {
    fun getPublishPerformer(): E
}