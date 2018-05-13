package playwrigkt.skript.example

import playwrigkt.skript.Skript
import playwrigkt.skript.application.*
import playwrigkt.skript.ex.andThen
import playwrigkt.skript.ex.join
import playwrigkt.skript.ex.lift
import playwrigkt.skript.file.*
import playwrigkt.skript.iostream.*
import playwrigkt.skript.performer.FilePerformer
import playwrigkt.skript.performer.SerializePerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.stagemanager.SyncJacksonSerializeStageManager
import playwrigkt.skript.troupe.FileTroupe
import playwrigkt.skript.troupe.SerializeTroupe
import playwrigkt.skript.troupe.SyncFileTroupe
import java.util.concurrent.CountDownLatch

data class MyTroupe(val inputStreamTroupe: InputStreamTroupe,
                    val outputStreamTroupe: OutputStreamTroupe,
                    val fileTroupe: FileTroupe,
                    val serializeTroupe: SerializeTroupe): FileTroupe, InputStreamTroupe, OutputStreamTroupe, SerializeTroupe {
    override fun getInputStreamPerformer(): AsyncResult<out InputStreamPerformer> = inputStreamTroupe.getInputStreamPerformer()

    override fun getOutputStreamPerformer(): AsyncResult<out OutputStreamPerformer> = outputStreamTroupe.getOutputStreamPerformer()

    override fun getSerializePerformer(): AsyncResult<out SerializePerformer> = serializeTroupe.getSerializePerformer()

    override fun getFilePerformer(): AsyncResult<out FilePerformer> = fileTroupe.getFilePerformer()
}


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
    val registry  = ApplicationRegistry()
    val waitLatch = CountDownLatch(1)

    val result = loadApplication.run("application.json", SkriptApplicationLoader(SyncFileTroupe, SyncJacksonSerializeStageManager().hireTroupe(), registry))
            .map { it.applicationResources.get("exampleApp") }
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
                println("closed manager")}

}