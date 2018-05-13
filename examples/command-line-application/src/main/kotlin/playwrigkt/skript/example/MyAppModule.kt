package playwrigkt.skript.example

import playwrigkt.skript.application.ApplicationResourceLoader
import playwrigkt.skript.application.SkriptModule

class MyAppModule: SkriptModule {
    override fun loaders(): List<ApplicationResourceLoader<*>> =
            listOf(MyStageManagerLoader)
}