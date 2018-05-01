package playwrigkt.skript.result

/**
 * Notifies a result of its completion.
 */
interface Completable<T> {
    /**
     * This has succeeded.
     */
    fun succeed(t: T)

    /**
     * This has failed
     */
    fun fail(error: Throwable)


    /**
     * return a function that completes this depending on a Result object
     */
    fun completionHandler(): ResultHandler<T> = {
        when(it) {
            is Result.Failure -> fail(it.error)
            is Result.Success -> succeed(it.result)
        }
    }
}