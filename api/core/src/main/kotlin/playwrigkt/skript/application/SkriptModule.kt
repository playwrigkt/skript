package playwrigkt.skript.application

interface SkriptModule {
        //TODO load venues
        fun loaders(): List<ApplicationResourceLoader<*>>
}