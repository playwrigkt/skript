package playwright.skript.stage

import playwright.skript.performer.PublishPerformer

interface PublishCast {
    fun getPublishPerformer(): PublishPerformer
}