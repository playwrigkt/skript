package playwrigkt.skript.example

import playwrigkt.skript.application.SkriptModule
import playwrigkt.skript.application.StageManagerLoader

class MyAppModule: SkriptModule {
    override fun loaders(): List<StageManagerLoader<*>> =
            listOf(MyStageManagerTroupeLoader)
}