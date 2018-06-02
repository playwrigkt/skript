package playwrigkt.skript.performer

import playwrigkt.skript.ex.toAsyncResult
import playwrigkt.skript.file.FileReference
import playwrigkt.skript.result.AsyncResult
import java.io.BufferedReader
import java.io.BufferedWriter
import java.nio.file.Files
import java.nio.file.StandardOpenOption

object SyncFilePerformer: FilePerformer {
    override fun create(i: FileReference): AsyncResult<FileReference> =
        i.toPath()
                .map { Files.createFile(it) }
                .map { i }
                .toAsyncResult()

    override fun reader(i: FileReference): AsyncResult<BufferedReader> =
            i.toPath()
                    .map(Files::newBufferedReader)
                    .toAsyncResult()

    override fun writer(i: FileReference): AsyncResult<BufferedWriter> =
            i.toPath()
                    .map { path -> Files.newBufferedWriter(path, StandardOpenOption.CREATE) }
                    .toAsyncResult()
}