package playwright.skript.venue

import playwright.skript.result.AsyncResult

interface Venue<C> {
    fun provideStage(): AsyncResult<C>
}