package playwrigkt.skript.venue

import playwrigkt.skript.result.AsyncResult

interface Venue<out Stage> {
    fun provideStage(): AsyncResult<out Stage>
}