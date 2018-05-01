package playwrigkt.skript.file

import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.andThen
import playwrigkt.skript.ex.toAsyncResult
import playwrigkt.skript.result.AsyncResult
import java.io.BufferedReader
import java.io.BufferedWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

interface FilePerformer {
    companion object {
        operator fun invoke(): FilePerformer = object: FilePerformer {
            override fun reader(i: FileReference): AsyncResult<BufferedReader> =
                    i.toPath()
                            .map(Files::newBufferedReader)
                            .toAsyncResult()

            override fun writer(i: FileReference): AsyncResult<BufferedWriter> =
                    i.toPath()
                            .map { path -> Files.newBufferedWriter(path, StandardOpenOption.CREATE) }
                            .toAsyncResult()
        }
    }

    fun reader(i: FileReference): AsyncResult<BufferedReader>

    fun writer(i: FileReference): AsyncResult<BufferedWriter>
}

interface FileTroupe {
    companion object {
        operator fun invoke(): FileTroupe = object: FileTroupe {
            val performer by lazy { AsyncResult.succeeded(FilePerformer()) }
            override fun getFilePerformer(): AsyncResult<FilePerformer> = performer
        }
    }

    fun getFilePerformer(): AsyncResult<FilePerformer>
}


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

fun <I, Troupe: FileTroupe> Skript<I, out FileReference, Troupe>.readFile(): Skript<I, BufferedReader, Troupe> =
        this.andThen(FileSkript.Reader)

fun <I, Troupe: FileTroupe> Skript<I, out FileReference, Troupe>.writeFile(): Skript<I, BufferedWriter, Troupe> =
        this.andThen(FileSkript.Writer)

sealed class FileReference {
    abstract fun toPath(): Try<Path>
    data class Relative(val path: String): FileReference() {
        override fun toPath(): Try<Path> {
            return Try { Paths.get(path) }
        }

    }
}