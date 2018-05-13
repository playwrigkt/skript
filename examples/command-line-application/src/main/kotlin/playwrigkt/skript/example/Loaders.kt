package playwrigkt.skript.example

import playwrigkt.skript.Skript
import playwrigkt.skript.application.SkriptApplicationLoader
import playwrigkt.skript.application.StageManagerLoader
import playwrigkt.skript.ex.all
import playwrigkt.skript.ex.join
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.iostream.CoroutineInputStreamTroupe
import playwrigkt.skript.iostream.CoroutineOutputStreamTroupe
import playwrigkt.skript.iostream.InputStreamTroupe
import playwrigkt.skript.iostream.OutputStreamTroupe
import playwrigkt.skript.troupe.FileTroupe
import playwrigkt.skript.troupe.SerializeTroupe

object MyStageManagerTroupeLoader: StageManagerLoader<MyTroupe> {
    override val loadManager: Skript<StageManagerLoader.Input, out StageManager<MyTroupe>, SkriptApplicationLoader> =
            Skript.identity<StageManagerLoader.Input, SkriptApplicationLoader>()
                    .all(
                            loadExistingStageManagerSkript<SerializeTroupe>("serialize"),
                            loadExistingStageManagerSkript<FileTroupe>("file"),
                            loadExistingStageManagerSkript<InputStreamTroupe>("inputStream"),
                            loadExistingStageManagerSkript<OutputStreamTroupe>("outputStream")
                    )
                    .join { serialize, file, inputStream, outputStream ->
                        MyStageManager(serialize, file, inputStream, outputStream)
                    }

    override val dependencies: List<String> = listOf("serialize", "file", "inputStream", "outputStream")
    override val name: String = "exampleApp"
}

object StdInStageManagerLoader: StageManagerLoader<InputStreamTroupe> {

    override val dependencies: List<String> = emptyList()
    override val name: String = "stdIn"

    override val loadManager: Skript<StageManagerLoader.Input, out StageManager<InputStreamTroupe>, SkriptApplicationLoader> =
            Skript.map { object : StageManager<InputStreamTroupe> {
                override fun tearDown(): AsyncResult<Unit> = AsyncResult.succeeded(Unit)
                override fun hireTroupe(): InputStreamTroupe = CoroutineInputStreamTroupe(System.`in`)
            } }
}

object StdOutStageManagerLoader: StageManagerLoader<OutputStreamTroupe> {
    override val dependencies: List<String> = emptyList()
    override val name: String = "stdOut"

    override val loadManager: Skript<StageManagerLoader.Input, out StageManager<OutputStreamTroupe>, SkriptApplicationLoader> =
            Skript.map {
                object : StageManager<OutputStreamTroupe> {
                    override fun tearDown(): AsyncResult<Unit> = AsyncResult.succeeded(Unit)
                    override fun hireTroupe(): OutputStreamTroupe = CoroutineOutputStreamTroupe(System.`out`)
                }
            }
}

