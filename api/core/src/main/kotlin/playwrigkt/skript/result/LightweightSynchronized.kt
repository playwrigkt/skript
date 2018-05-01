package playwrigkt.skript.result

import java.util.concurrent.locks.ReentrantLock

/**
 * Simple interface for synchronizing a function without using the synchronized keyword.
 *
 * The synchronized keyword requires that low level hardward caches be flushed, which can hurt performance
 * on multithreaded systems intending to take advantage of multiple cores.
 */
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