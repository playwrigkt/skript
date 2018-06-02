package playwrigkt.skript.example

import playwrigkt.skript.Skript
import playwrigkt.skript.application.ApplicationRegistry
import playwrigkt.skript.application.createApplication
import playwrigkt.skript.ex.andThen
import playwrigkt.skript.ex.join
import playwrigkt.skript.ex.lift
import playwrigkt.skript.file.FileReference
import playwrigkt.skript.file.FileSkript
import playwrigkt.skript.iostream.*
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.troupe.FileTroupe
import playwrigkt.skript.troupe.SerializeTroupe
import java.util.concurrent.CountDownLatch

data class MyTroupe(val inputStreamTroupe: InputStreamTroupe,
                    val outputStreamTroupe: OutputStreamTroupe,
                    val fileTroupe: FileTroupe,
                    val serializeTroupe: SerializeTroupe):
        FileTroupe by fileTroupe,
        InputStreamTroupe by inputStreamTroupe,
        OutputStreamTroupe by outputStreamTroupe,
        SerializeTroupe by serializeTroupe


data class MyStageManager(val serialize: StageManager<SerializeTroupe>,
                          val file: StageManager<FileTroupe>,
                          val inputStream: StageManager<InputStreamTroupe>,
                          val outputStream: StageManager<OutputStreamTroupe>): StageManager<MyTroupe> {
    override fun hireTroupe(): MyTroupe =
            MyTroupe(inputStream.hireTroupe(), outputStream.hireTroupe(), file.hireTroupe(), serialize.hireTroupe())

    override fun tearDown(): AsyncResult<Unit> =
            listOf(serialize.tearDown(), file.tearDown(), inputStream.tearDown(), outputStream.tearDown())
                    .lift()
                    .map { Unit}
}

val applicationSkript = Skript.identity<Unit, MyTroupe>()
        .map { "What is your message?"}
        .andThen(IOStreamSkript.WriteLine)
        .andThen(IOStreamSkript.ReadLine)
        .split(Skript.identity<String, MyTroupe>()
                .map { "where should I put  it?" }
                .andThen(IOStreamSkript.WriteLine)
                .andThen(IOStreamSkript.ReadLine)
                .map { FileReference.Relative(it) }
                .andThen(FileSkript.Writer))
        .join { content, writer ->
            writer.appendln(content)
            writer.flush()
            writer.close()
        }
        .map { "Okay, I've finished recording!" }
        .andThen(IOStreamSkript.WriteLine)

fun main(args: Array<String>) {
    val waitLatch = CountDownLatch(1)

    val result = createApplication("examples/command-line-application/application.json")
            .map { it.applicationResources.get(MyStageManagerLoader.name()) }
            .flatMap { it
                    ?.let { AsyncResult.succeeded(it) }
                    ?: AsyncResult.failed(
                            ApplicationRegistry.RegistryException(ApplicationRegistry.RegistryError.NotFound("exampleApp")))
            }
            .map { it as MyStageManager }

    result
            .flatMap { applicationSkript.run(Unit, it.hireTroupe()) }
            .addHandler { waitLatch.countDown() }

    waitLatch.await()
    result.flatMap { it.tearDown() }
            .addHandler {
                println(it)
                println("closed manager")}

}