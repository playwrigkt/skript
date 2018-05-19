package playwrigkt.skript.example

import playwrigkt.skript.application.ApplicationResourceLoader
import playwrigkt.skript.application.SkriptModule

class StdIOModule: SkriptModule {
    override fun loaders(): List<ApplicationResourceLoader<*>> =
            listOf(StdInStageManagerLoader, StdOutStageManagerLoader)
}