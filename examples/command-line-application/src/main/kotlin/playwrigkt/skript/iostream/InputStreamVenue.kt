package playwrigkt.skript.iostream

import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult

interface InputStreamPerformer {
    fun readLine(): AsyncResult<String>
}


interface OutputStreamPerformer {
    fun write(text: String): AsyncResult<Unit>
    fun writeLine(text: String): AsyncResult<Unit>
}



interface InputStreamTroupe {
    fun getInputStreamPerformer(): AsyncResult<out InputStreamPerformer>
}

interface OutputStreamTroupe {
    fun getOutputStreamPerformer(): AsyncResult<out OutputStreamPerformer>
}

sealed class IOStreamSkript<I, O, Troupe>: Skript<I, O, Troupe> {
    object ReadLine: IOStreamSkript<Any, String, InputStreamTroupe>() {
        override fun run(i: Any, troupe: InputStreamTroupe): AsyncResult<String> =
                troupe.getInputStreamPerformer()
                        .flatMap { ioStreamPerformer -> ioStreamPerformer.readLine() }
    }

    object WriteLine: IOStreamSkript<String, Unit, OutputStreamTroupe>() {
        override fun run(i: String, troupe: OutputStreamTroupe): AsyncResult<Unit> =
                troupe.getOutputStreamPerformer()
                        .flatMap { it.writeLine(i) }
    }

    object Write: IOStreamSkript<String, Unit, OutputStreamTroupe>() {
        override fun run(i: String, troupe: OutputStreamTroupe): AsyncResult<Unit> =
                troupe.getOutputStreamPerformer()
                        .flatMap { it.write(i) }
    }
}


