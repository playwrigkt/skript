package playwrigkt.skript.application

class SyncCoreSkriptModule: SkriptModule {
    override fun loaders(): List<ApplicationResourceLoader<*>> =
            listOf(SyncFileStageManagerLoader, SyncJacksonSerializeStageManagerLoader)
}