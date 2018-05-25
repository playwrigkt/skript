package playwrigkt.skript.result

import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.ReentrantLock

/**
 * Implementation of AsyncResult used by executors of Asynchronous Computation.  This class should not be used as the
 * return type of public methods
 */
interface CompletableResult<T>: AsyncResult<T>, Completable<T> {
    companion object {
        /**
         * Create a new completable result
         */
        operator fun <T> invoke(): CompletableResult<T> = CompletableResultImpl()

        /**
         * Synchronously create a completableResult that has already been completed
         */
        fun <T> succeeded(t: T): CompletableResult<T> {
            val result = CompletableResultImpl<T>()
            result.succeed(t)
            return result
        }

        /**
         * Synchronously create a completableResult that has already failed
         */
        fun <T> failed(error: Throwable): CompletableResult<T> {
            val result = CompletableResultImpl<T>()
            result.fail(error)
            return result
        }
    }

    private class CompletableResultImpl<T> : CompletableResult<T>, LightweightSynchronized {
        @Volatile private var result: Result<T>? = null
        @Volatile private var handlers: Queue<ResultHandler<T>> = LinkedBlockingQueue()
        override val lock: ReentrantLock = ReentrantLock()

        override fun <U> map(f: (T) -> U): AsyncResult<U> {
            val newResult = CompletableResultImpl<U>()
            addHandler {
                when (it) {
                    is Result.Failure -> newResult.fail(it.error)
                    is Result.Success -> {
                        try {
                            newResult.succeed(f(it.result))
                        } catch(e: Throwable) {
                            newResult.fail(e)
                        }
                    }
                }
            }
            return newResult
        }

        override fun <U> flatMap(f: (T) -> AsyncResult<out U>): AsyncResult<U> {
            val newResult: CompletableResult<U> = CompletableResultImpl()
            addHandler {
                when(it) {
                    is Result.Failure -> newResult.fail(it.error)
                    is Result.Success ->
                        f(it.result).addHandler { asyncResult ->
                            when(asyncResult) {
                                is Result.Failure -> newResult.fail(asyncResult.error)
                                is Result.Success -> newResult.succeed(asyncResult.result)
                            }
                        }
                }
            }
            return newResult
        }

        override fun recover(f: (Throwable) -> AsyncResult<out T>): AsyncResult<T> {
            val newResult: CompletableResult<T> = invoke()
            addHandler {
                when(it) {
                    is Result.Failure ->
                        f(it.error).addHandler { asyncResult ->
                            when(asyncResult) {
                                is Result.Failure -> newResult.fail(asyncResult.error)
                                is Result.Success -> newResult.succeed(asyncResult.result)
                            }
                        }
                    is Result.Success -> newResult.succeed(it.result)
                }
            }
            return newResult
        }

        override fun onSuccess(f: (T) -> Unit): AsyncResult<T> {
            addHandler {
                when (it) {
                    is Result.Failure -> { }
                    is Result.Success -> {
                        try {
                            f(it.result)
                        } catch(e: Throwable) {

                        }
                    }
                }
            }
            return this
        }

        override fun onFailure(f: (Throwable) -> Unit): AsyncResult<T> {
            addHandler {
                when (it) {
                    is Result.Failure -> {
                        try {
                            f(it.error)
                        } catch (e: Throwable) {

                        }
                    }
                    is Result.Success -> { }
                }
            }
            return this
        }

        override fun addHandler(handler: (Result<T>) -> Unit): Unit = lock {
            this.result?.let(handler)
                    ?: handlers.add(handler)
        }

        override fun succeed(t: T): Unit {
            lock {
                if(!isComplete()) {
                    result = Result.Success(t)
                } else {
                    throw IllegalStateException("Result is already complete")
                }
            }
            handlers.map { it.invoke(Result.Success(t)) }
        }

        override fun fail(error: Throwable): Unit {
            lock {
                if(!isComplete()) {
                    result = Result.Failure(error)
                } else {
                    throw IllegalStateException("Result is already complete")
                }
            }
            handlers.map { it(Result.Failure(error)) }
        }

        override fun isComplete(): Boolean {
            return result != null
        }

        override fun isSuccess(): Boolean {
            return result?.isSuccess?:false
        }

        override fun isFailure(): Boolean {
            return result?.isSuccess?.not()?:false
        }

        override fun result(): T? {
            return result?.result
        }

        override fun error(): Throwable? {
            return result?.error
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is CompletableResultImpl<*>) return false

            if (result != other.result) return false

            return true
        }

        override fun hashCode(): Int {
            var result1 = result?.hashCode() ?: 0
            result1 = 31 * result1 + (handlers.hashCode())
            return result1
        }

        override fun toString(): String {
            return "CompletableResultImpl(result=$result, handlers=$handlers    )"
        }
    }
}