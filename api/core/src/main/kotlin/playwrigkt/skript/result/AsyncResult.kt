package playwrigkt.skript.result

/**
 * A computation that is being run asynchronously, or has already completed
 */
interface AsyncResult<T> {
    companion object {
        fun <T> succeeded(t: T): AsyncResult<T> = CompletableResult.succeeded(t)
        fun <T> failed(error: Throwable): AsyncResult<T> = CompletableResult.failed(error)
    }

    /**
     * Once this computation succeeds run a synchronous function, or run it now if this computation has already
     * succeeded
     */
    fun <U> map(f: (T) -> U): AsyncResult<U>

    /**
     * Once this computation succeeds run an asynchronous function, or run it now if this computation has already
     * succeeded
     */
    fun <U> flatMap(f: (T) -> AsyncResult<out U>): AsyncResult<U>

    /**
     * If this computation fails, run an asynchronous function, or run it now if this computation has already failed
     */
    fun recover(f: (Throwable) -> AsyncResult<out T>): AsyncResult<T>

    /**
     * If this computation succeeds, run a synchronous function, or run  it now if this computation has already
     * succeeded
     */
    fun onSuccess(f: (T) -> Unit): AsyncResult<T>

    /**
     * If this computation fails, run a synchronous function, or run it now if this computation has already failed
     */
    fun onFailure(f: (Throwable) -> Unit): AsyncResult<T>

    /**
     * Call a function when this computation is finished, or call it now if this computation has already  finished
     */
    fun addHandler(handler: (Result<T>) -> Unit)


    fun <U> also(f: (Result<T>) -> AsyncResult<U>): AsyncResult<U> {
        val result = CompletableResult<U>()
        this.addHandler {
            f(it).addHandler(result.completionHandler())
        }
        return result
    }

    /**
     * Return true if the computation finished either successfully or with an error
     */
    fun isComplete(): Boolean

    /**
     * Return true if this computation finished with an error
     */
    fun isSuccess(): Boolean

    /**
     * @return true if this computation finished with an error
     */
    fun isFailure(): Boolean

    /**
     * The result if this computation finished successfully
     */
    fun result(): T?

    /**
     * The result if this  computation  finished with  an error
     */
    fun error(): Throwable?
}