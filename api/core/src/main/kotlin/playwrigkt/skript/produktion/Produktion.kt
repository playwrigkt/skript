package playwrigkt.skript.produktion

import playwrigkt.skript.result.AsyncResult

/**
 * A running process that receives input and runs a skript with that input.
 */
interface Produktion {
    /**
     * @return true if this Produktion is still listening for input
     */
    fun isRunning(): Boolean

    /**
     * Stop this produktion
     */
    fun stop(): AsyncResult<Unit>

    /**
     * A result that is completed once this Produktion has stopped listening
     */
    fun result(): AsyncResult<Unit>
}