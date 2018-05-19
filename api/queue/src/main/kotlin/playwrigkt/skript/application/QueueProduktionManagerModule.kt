package playwrigkt.skript.application

class QueueProduktionManagerModule: SkriptModule {
    override fun loaders(): List<ApplicationResourceLoader<*>> = listOf(QueueProduktionManagerLoader)
}