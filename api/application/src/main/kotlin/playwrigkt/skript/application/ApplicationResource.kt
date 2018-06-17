package playwrigkt.skript.application

import playwrigkt.skript.result.AsyncResult

interface ApplicationResource {
    /**
     * Destroy this resource and the resources it manages
     */
    fun tearDown(): AsyncResult<Unit>
}