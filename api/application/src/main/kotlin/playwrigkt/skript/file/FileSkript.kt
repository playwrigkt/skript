package playwrigkt.skript.file

import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.FileTroupe
import java.io.BufferedReader
import java.io.BufferedWriter

sealed class FileSkript<I, O>: Skript<I, O, FileTroupe> {
    object Reader: FileSkript<FileReference, BufferedReader>() {
        override fun run(i: FileReference, troupe: FileTroupe): AsyncResult<BufferedReader> =
                troupe.getFilePerformer().flatMap { it.reader(i) }
    }

    object Writer: FileSkript<FileReference, BufferedWriter>() {
        override fun run(i: FileReference, troupe: FileTroupe): AsyncResult<BufferedWriter> =
                troupe.getFilePerformer().flatMap { it.writer(i) }

    }

    object Create: FileSkript<FileReference, FileReference>() {
        override fun run(i: FileReference, troupe: FileTroupe): AsyncResult<FileReference> =
            troupe.getFilePerformer().flatMap { it.create(i) }
    }
}

