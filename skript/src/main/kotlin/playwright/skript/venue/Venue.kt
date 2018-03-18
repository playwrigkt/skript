package playwright.skript.venue

import playwright.skript.result.AsyncResult

interface Venue<Stage> {
    fun provideStage(): AsyncResult<Stage>
}