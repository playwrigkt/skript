package playwrigkt.skript.stagemanager

import playwrigkt.skript.application.ApplicationResource


/**
 * Provisions a Troupe when a skript is run.  Scopes application resources.
 */
interface StageManager<out Troupe>: ApplicationResource {
    /**
     * Create a Troupe for use in a skript
     */
    fun hireTroupe(): Troupe
}