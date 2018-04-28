package playwrigkt.skript.file

import playwrigkt.skript.Skript
import playwrigkt.skript.ex.andThen
import playwrigkt.skript.ex.join
import playwrigkt.skript.ex.lift
import playwrigkt.skript.performer.SerializePerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.JacksonSerializeStageManager
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.troupe.JacksonSerializeTroupe
import playwrigkt.skript.troupe.SerializeTroupe
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

data class MyTroupe(val ioStreamTroupe: IOStreamTroupe, val fileTroupe: FileTroupe, val serializeTroupe: SerializeTroupe): FileTroupe, IOStreamTroupe, SerializeTroupe {
    override fun getSerializePerformer(): AsyncResult<out SerializePerformer> = serializeTroupe.getSerializePerformer()

    override fun getFilePerformer(): AsyncResult<FilePerformer> = fileTroupe.getFilePerformer()

    override fun getIOStreamPerformer(): AsyncResult<IOStreamPerformer> =ioStreamTroupe.getIOStreamPerformer()
}


data class MyStageManager(val serialize: StageManager<SerializeTroupe>,
                          val file: StageManager<FileTroupe>,
                          val ioStream: StageManager<IOStreamTroupe>): StageManager<MyTroupe> {
    override fun hireTroupe(): MyTroupe =
            MyTroupe(ioStream.hireTroupe(), file.hireTroupe(), serialize.hireTroupe())

    override fun tearDown(): AsyncResult<Unit> =
            listOf(serialize.tearDown(), file.tearDown(), ioStream.tearDown())
                    .lift()
                    .map { Unit}
}


fun main(args: Array<String>) {
    val ioStream = IOStream(BufferedReader(InputStreamReader(System.`in`)), BufferedWriter(OutputStreamWriter(System.`out`)))

    val ioStreamTroupe = IOStreamTroupe(ioStream)
    val fileTroupe = FileTroupe()
    val serializeTroupe = JacksonSerializeTroupe(JacksonSerializeStageManager.defaultObjectMapper)
    val troupe = MyTroupe(ioStreamTroupe, fileTroupe, serializeTroupe)

    val running =
            Skript.identity<Unit, MyTroupe>()
                    .andThen(IOStreamSkript.Read("What is your message?"))
                    .split(Skript.identity<String, MyTroupe>()
                            .map { Unit }
                            .andThen(IOStreamSkript.Read("Where shall I put it?"))
                            .map { FileReference.Relative(it) }
                            .andThen(FileSkript.Writer))
                    .join { content, writer ->
                        writer.appendln(content)
                        writer.flush()
                        writer.close()
                    }
            .map { "Okay, I've finished recording!" }
            .andThen(IOStreamSkript.Write)
            .run(Unit, troupe)

    while(!running.isComplete()) {
        Thread.sleep(100)
    }
    println(ioStream.close())
}