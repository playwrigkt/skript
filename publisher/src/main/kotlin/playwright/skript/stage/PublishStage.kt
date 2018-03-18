package playwright.skript.stage

import playwright.skript.performer.PublishPerformer

interface PublishStage {
    fun getPublishPerformer(): PublishPerformer
}