package dev.yn.playground.coroutine

import dev.yn.playground.Skript
import dev.yn.playground.result.AsyncResult
import dev.yn.playground.result.CompletableResult
import kotlinx.coroutines.experimental.launch

data class CoroutineSkript<I, O, C>(val f: suspend (I) -> O): Skript<I, O, C> {
    override fun run(i: I, context: C): AsyncResult<O> {
        val asyncResult = CompletableResult<O>()
        launch {
            try {
                val out = f(i)
                asyncResult.succeed(out)
            } catch(e: Throwable) {
                asyncResult.fail(e)
            }
        }
        return asyncResult
    }
}