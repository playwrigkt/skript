package playwrigkt.skript.file

import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.FileTroupe
import java.io.BufferedReader
import java.io.BufferedWriter
import java.nio.file.Path
import java.nio.file.Paths

sealed class FileSkript<I, O>: Skript<I, O, FileTroupe> {
    object Reader: FileSkript<FileReference, BufferedReader>() {
        override fun run(i: FileReference, troupe: FileTroupe): AsyncResult<BufferedReader> =
                troupe.getFilePerformer().flatMap { it.reader(i) }
    }

    object Writer: FileSkript<FileReference, BufferedWriter>() {
        override fun run(i: FileReference, troupe: FileTroupe): AsyncResult<BufferedWriter> =
                troupe.getFilePerformer().flatMap { it.writer(i) }

    }
}

sealed class FileReference {
    abstract fun toPath(): Try<Path>
    data class Relative(val path: String): FileReference() {
        override fun toPath(): Try<Path> {
            return Try { Paths.get(path) }
        }
    }
    data class Absolute(val path: String): FileReference() {
        override fun toPath(): Try<Path> {
            return Try { Paths.get(path) }
        }

    }
}