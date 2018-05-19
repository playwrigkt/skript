package playwrigkt.skript.application

class HttpProduktionManagerModule: SkriptModule {
    override fun loaders(): List<ApplicationResourceLoader<*>> =
        listOf(HttpProduktionManagerLoader)
}