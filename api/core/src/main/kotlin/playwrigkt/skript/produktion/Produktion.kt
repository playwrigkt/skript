package playwrigkt.skript.produktion

import playwrigkt.skript.result.AsyncResult

interface Produktion {
    fun isRunning(): Boolean
    fun stop(): AsyncResult<Unit>
    fun result(): AsyncResult<Unit>
}