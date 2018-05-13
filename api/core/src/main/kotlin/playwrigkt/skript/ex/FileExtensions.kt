package playwrigkt.skript.ex

import playwrigkt.skript.Skript
import playwrigkt.skript.file.FileReference
import playwrigkt.skript.file.FileSkript
import playwrigkt.skript.troupe.FileTroupe
import java.io.BufferedReader
import java.io.BufferedWriter

fun <I, Troupe: FileTroupe> Skript<I, out FileReference, Troupe>.readFile(): Skript<I, BufferedReader, Troupe> =
        this.andThen(FileSkript.Reader)

fun <I, Troupe: FileTroupe> Skript<I, out FileReference, Troupe>.writeFile(): Skript<I, BufferedWriter, Troupe> =
        this.andThen(FileSkript.Writer)

fun <I, Troupe: FileTroupe> Skript<I, out FileReference, Troupe>.createFile(): Skript<I, out FileReference, Troupe> =
        this.andThen(FileSkript.Create)