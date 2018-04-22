package playwrigkt.skript.stagemanager

import playwrigkt.skript.result.AsyncResult

/**
 * Provisions a Troupe when a skript is run.  Scopes application resources.
 */
interface StageManager<out Troupe> {
    /**
     * Create a Troupe for use in a skript
     */
    fun hireTroupe(): Troupe

    /**
     * Destroy resources created by this StageManager
     */
    fun tearDown(): AsyncResult<Unit>
}