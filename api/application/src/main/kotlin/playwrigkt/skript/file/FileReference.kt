package playwrigkt.skript.file

import arrow.core.Try
import java.nio.file.Path
import java.nio.file.Paths

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