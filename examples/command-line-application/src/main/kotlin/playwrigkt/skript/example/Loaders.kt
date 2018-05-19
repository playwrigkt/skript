package playwrigkt.skript.example

import playwrigkt.skript.Skript
import playwrigkt.skript.application.ApplicationResourceLoader
import playwrigkt.skript.application.SkriptApplicationLoader
import playwrigkt.skript.ex.all
import playwrigkt.skript.ex.join
import playwrigkt.skript.iostream.CoroutineInputStreamTroupe
import playwrigkt.skript.iostream.CoroutineOutputStreamTroupe
import playwrigkt.skript.iostream.InputStreamTroupe
import playwrigkt.skript.iostream.OutputStreamTroupe
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.troupe.FileTroupe
import playwrigkt.skript.troupe.SerializeTroupe

object MyStageManagerLoader: ApplicationResourceLoader<MyStageManager> {
    override val loadResource: Skript<ApplicationResourceLoader.Input, MyStageManager, SkriptApplicationLoader> =
            Skript.identity<ApplicationResourceLoader.Input, SkriptApplicationLoader>()
                    .all(
                            loadExistingApplicationResourceSkript<StageManager<SerializeTroupe>>("serialize"),
                            loadExistingApplicationResourceSkript<StageManager<FileTroupe>>("file"),
                            loadExistingApplicationResourceSkript<StageManager<InputStreamTroupe>>("inputStream"),
                            loadExistingApplicationResourceSkript<StageManager<OutputStreamTroupe>>("outputStream")
                    )
                    .join { serialize, file, inputStream, outputStream ->
                        MyStageManager(serialize, file, inputStream, outputStream)
                    }

    override val dependencies: List<String> = listOf("serialize", "file", "inputStream", "outputStream")
}

object StdInStageManagerLoader: ApplicationResourceLoader<StageManager<InputStreamTroupe>> {

    override val dependencies: List<String> = emptyList()

    override val loadResource: Skript<ApplicationResourceLoader.Input, StageManager<InputStreamTroupe>, SkriptApplicationLoader> =
            Skript.map { object : StageManager<InputStreamTroupe> {
                override fun tearDown(): AsyncResult<Unit> = AsyncResult.succeeded(Unit)
                override fun hireTroupe(): InputStreamTroupe = CoroutineInputStreamTroupe(System.`in`)
            } }
}

object StdOutStageManagerLoader: ApplicationResourceLoader<StageManager<OutputStreamTroupe>> {
    override val dependencies: List<String> = emptyList()

    override val loadResource: Skript<ApplicationResourceLoader.Input, StageManager<OutputStreamTroupe>, SkriptApplicationLoader> =
            Skript.map {
                object : StageManager<OutputStreamTroupe> {
                    override fun tearDown(): AsyncResult<Unit> = AsyncResult.succeeded(Unit)
                    override fun hireTroupe(): OutputStreamTroupe = CoroutineOutputStreamTroupe(System.`out`)
                }
            }
}

