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



//class InputVenue: Venue<InputStream, String, String>() {
//    override fun <Troupe> createProduktion(skript: Skript<String, String, Troupe>, stageManager: StageManager<Troupe>, rule: IOStream): AsyncResult<out Produktion> {
//        return Try { InputProduktion(rule, stageManager, skript) }
//                .toAsyncResult()
//    }
//
//    override fun stop(): AsyncResult<Unit> = AsyncResult.succeeded(Unit)
//}
//
//data class InputProduktion<Troupe>(
//        val ioStream: IOStream,
//        val stageManager: StageManager<Troupe>,
//        val skript: Skript<String, String, Troupe>): Produktion, LightweightSynchronized {
//    override val lock: ReentrantLock = ReentrantLock()
//    val result = CompletableResult<Unit>()
//
//    init {
//        runAsync {
//            while(isRunning()) {
//                handleLine()
//            }
//        }
//    }
//
//    fun handleLine(): AsyncResult<Unit> =
//            readLine()
//                    .toAsyncResult()
//                    .flatMap { skript.run(it, stageManager.hireTroupe()) }
//                    .map(ioStream::write)
//
//    private fun readLine(): Try<String> = lock { Try { ioStream.readLine() } }
//
//    override fun isRunning(): Boolean = result.isComplete()
//
//    override fun stop(): AsyncResult<Unit> = lock {
//        if(result.isComplete()) {
//            result
//        } else {
//            ioStream.close()
//                    .onSuccess(result::succeed)
//                    .onFailure(result::fail)
//            result
//        }
//    }
//
//    override fun result(): AsyncResult<Unit> = result
//}
