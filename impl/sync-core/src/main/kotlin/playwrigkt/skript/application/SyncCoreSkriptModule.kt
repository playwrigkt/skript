package playwrigkt.skript.application

class SyncCoreSkriptModule: SkriptModule {
    override fun loaders(): List<StageManagerLoader<*>> =
            listOf(SyncFileStageManagerLoader, SyncJacksonSerializeStageManagerLoader)
}