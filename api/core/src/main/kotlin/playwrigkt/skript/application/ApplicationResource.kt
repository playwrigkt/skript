package playwrigkt.skript.application

import playwrigkt.skript.result.AsyncResult

//TODO make part of ApplicationRegistry, ApplicationResourceLoader
//TODO use in  shutdown hook of applicationResources
interface ApplicationResource {
    /**
     * Destroy this resource and the resources it manages
     */
    fun tearDown(): AsyncResult<Unit>
}