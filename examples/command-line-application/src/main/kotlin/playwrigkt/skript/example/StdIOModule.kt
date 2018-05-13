package playwrigkt.skript.example

import playwrigkt.skript.application.SkriptModule
import playwrigkt.skript.application.ApplicationResourceLoader

class StdIOModule: SkriptModule {
    override fun loaders(): List<ApplicationResourceLoader<*>> =
            listOf(StdInStageManagerLoader, StdOutStageManagerLoader)
}