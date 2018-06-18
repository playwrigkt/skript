package playwrigkt.skript.performer

import playwrigkt.skript.file.FileReference
import playwrigkt.skript.result.AsyncResult
import java.io.BufferedReader
import java.io.BufferedWriter

//TODO, expose create, write, writeCreateNew, writeCreateIfNotExists, truncate, append
interface FilePerformer {
    fun reader(i: FileReference): AsyncResult<BufferedReader>

    fun writer(i: FileReference): AsyncResult<BufferedWriter>

    fun create(i: FileReference): AsyncResult<FileReference>
}