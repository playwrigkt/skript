package playwrigkt.skript.result

import org.funktionale.tries.Try
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.ReentrantLock

fun <T> Try<T>.toAsyncResult(): AsyncResult<T> =
    when(this) {
        is Try.Success -> AsyncResult.succeeded(this.get())
        is Try.Failure -> AsyncResult.failed(this.throwable)
    }

interface AsyncResult<T> {
    companion object {
        fun <T> succeeded(t: T): AsyncResult<T> = CompletableResult.succeeded(t)
        fun <T> failed(error: Throwable): AsyncResult<T> = CompletableResult.failed(error)
    }

    fun <U> map(f: (T) -> U): AsyncResult<U>
    fun <U> flatMap(f: (T) -> AsyncResult<U>): AsyncResult<U>
    fun recover(f: (Throwable) -> AsyncResult<T>): AsyncResult<T>

    fun onSuccess(f: (T) -> Unit): AsyncResult<T>
    fun onFailure(f: (Throwable) -> Unit): AsyncResult<T>

    fun addHandler(handler: (Result<T>) -> Unit)
    fun alsoComplete(completableResult: CompletableResult<T>): Unit = this.addHandler {
        it.result?.let(completableResult::succeed)
                ?: it.error?.let(completableResult::fail)
    }

    fun isComplete(): Boolean
    fun isSuccess(): Boolean
    fun isFailure(): Boolean

    fun result(): T?
    fun error(): Throwable?
}

interface Completable<T> {
    fun succeed(t: T)
    fun fail(error: Throwable)

    fun completionHandler(): ResultHandler<T> = {
        when(it) {
            is Result.Failure -> fail(it.error)
            is Result.Success -> succeed(it.result)
        }
    }
}

interface LightweightSynchronized {
    val lock: ReentrantLock

    fun <T> lock(fn: () -> T): T {
        lock.lockInterruptibly()
        try {
            return fn()
        } finally {
            lock.unlock()
        }
    }
}
interface CompletableResult<T>: AsyncResult<T>, Completable<T> {
    companion object {
        operator fun <T> invoke(): CompletableResult<T> = CompletableResultImpl()
        fun <T> succeeded(t: T): CompletableResult<T> {
            val result = CompletableResultImpl<T>()
            result.succeed(t)
            return result
        }

        fun <T> failed(error: Throwable): CompletableResult<T> {
            val result = CompletableResultImpl<T>()
            result.fail(error)
            return result
        }
    }

    private class CompletableResultImpl<T>() : CompletableResult<T>, LightweightSynchronized {
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

        override fun <U> flatMap(f: (T) -> AsyncResult<U>): AsyncResult<U> {
            val newResult: CompletableResult<U> = CompletableResultImpl<U>()
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

        override fun recover(f: (Throwable) -> AsyncResult<T>): AsyncResult<T> {
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

        override fun succeed(t: T): Unit = lock {
            if(!isComplete()) {
                result = Result.Success(t)
                handlers.map { it.invoke(Result.Success(t)) }
            } else {
                throw IllegalStateException("Result is already succeed")
            }
        }

        override fun fail(error: Throwable): Unit = lock {
            if(!isComplete()) {
                result = Result.Failure(error)
                handlers.map { it(Result.Failure(error)) }
            } else {
                throw IllegalStateException("Result is already succeed")
            }
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