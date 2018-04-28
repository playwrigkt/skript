package playwrigkt.skript.file

import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.coroutine.runAsync
import playwrigkt.skript.ex.toAsyncResult
import playwrigkt.skript.produktion.Produktion
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.result.LightweightSynchronized
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.venue.Venue
import java.io.BufferedReader
import java.io.BufferedWriter
import java.util.concurrent.locks.ReentrantLock

data class IOStreamPerformer(val ioStream: IOStream) {
    fun read(): AsyncResult<String> = runAsync { ioStream.readLine() }
    fun write(message: String): AsyncResult<Unit> = runAsync { ioStream.write(message) }
}

interface IOStreamTroupe {
    companion object {
        operator fun invoke(ioStream: IOStream) = object: IOStreamTroupe {
            private val ioStreamPerformer by lazy { AsyncResult.succeeded(IOStreamPerformer(ioStream)) }
            override fun getIOStreamPerformer() = ioStreamPerformer
        }
    }
    fun getIOStreamPerformer(): AsyncResult<IOStreamPerformer>
}

sealed class IOStreamSkript<I, O>: Skript<I, O, IOStreamTroupe> {
    data class Read(val prompt: String): IOStreamSkript<Unit, String>() {
        override fun run(i: Unit, troupe: IOStreamTroupe): AsyncResult<String> =
            troupe.getIOStreamPerformer()
                    .flatMap { ioStreamPerformer ->
                        ioStreamPerformer.write(prompt)
                        ioStreamPerformer.read()
                    }
    }

    object Write: IOStreamSkript<String, Unit>() {
        override fun run(i: String, troupe: IOStreamTroupe): AsyncResult<Unit> =
            troupe.getIOStreamPerformer()
                    .flatMap { it.write(i) }
    }
}

data class IOStream(private val reader: BufferedReader, private val writer: BufferedWriter): LightweightSynchronized {
    override val lock: ReentrantLock = ReentrantLock()

    fun write(message: String): Unit = lock {
        writer.appendln(message)
        writer.flush()
    }

    fun readLine(): String = lock {
        reader.readLine()
    }

    fun close(): Try<Unit> = lock {
        Try { reader.close() }
                .map { writer.close() }
    }
}

class InputVenue: Venue<IOStream, String, String>() {
    override fun <Troupe> createProduktion(skript: Skript<String, String, Troupe>, stageManager: StageManager<Troupe>, rule: IOStream): AsyncResult<out Produktion> {
        return Try { InputProduktion(rule, stageManager, skript) }
                .toAsyncResult()
    }

    override fun stop(): AsyncResult<Unit> = AsyncResult.succeeded(Unit)
}

data class InputProduktion<Troupe>(
        val ioStream: IOStream,
        val stageManager: StageManager<Troupe>,
        val skript: Skript<String, String, Troupe>): Produktion, LightweightSynchronized {
    override val lock: ReentrantLock = ReentrantLock()
    val result = CompletableResult<Unit>()

    init {
        runAsync {
            while(isRunning()) {
                handleLine()
            }
        }
    }

    fun handleLine(): AsyncResult<Unit> =
        readLine()
                .toAsyncResult()
                .flatMap { skript.run(it, stageManager.hireTroupe()) }
                .map(ioStream::write)

    private fun readLine(): Try<String> = lock { Try { ioStream.readLine() } }

    override fun isRunning(): Boolean = result.isComplete()

    override fun stop(): AsyncResult<Unit> = lock {
        if(result.isComplete()) {
            result
        } else {
            ioStream.close()
                    .onSuccess(result::succeed)
                    .onFailure(result::fail)
            result
        }
    }

    override fun result(): AsyncResult<Unit> = result
}
