package playwright.skript.venue

import playwright.skript.result.AsyncResult

interface Venue<out Stage> {
    fun provideStage(): AsyncResult<out Stage>
}