package playwrigkt.skript.iostream

import playwrigkt.skript.coroutine.runAsync
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.LightweightSynchronized
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.locks.ReentrantLock

data class CoroutineInputStreamPerformer(val inputStream: InputStream): InputStreamPerformer, LightweightSynchronized {
    override val lock: ReentrantLock = ReentrantLock()
    val bufferedReader by lazy { lock { inputStream.bufferedReader() } }

    override fun readLine(): AsyncResult<String> = runAsync { lock { bufferedReader.readLine() } }
}



data class CoroutineOutputStreamPerformer(val outputStream: OutputStream): OutputStreamPerformer, LightweightSynchronized {
    override val lock: ReentrantLock = ReentrantLock()
    val bufferedWriter by lazy { lock { outputStream.bufferedWriter() } }

    override fun write(text: String): AsyncResult<Unit> = runAsync { lock {
        bufferedWriter.append(text)
        bufferedWriter.flush()
    } }

    override fun writeLine(text: String): AsyncResult<Unit> = runAsync { lock {
        bufferedWriter.appendln(text)
        bufferedWriter.flush()
    } }
}

data class CoroutineInputStreamTroupe(val inputStream: InputStream): InputStreamTroupe {
    private  val performer by lazy { AsyncResult.succeeded(CoroutineInputStreamPerformer(inputStream))}
    override fun getInputStreamPerformer(): AsyncResult<out InputStreamPerformer> = performer
}

data class CoroutineOutputStreamTroupe(val outputStream: OutputStream): OutputStreamTroupe {
    private val performer by lazy  { AsyncResult.succeeded(CoroutineOutputStreamPerformer(outputStream))}
    override fun getOutputStreamPerformer(): AsyncResult<out OutputStreamPerformer> = performer
}
