package playwrigkt.skript.application

interface SkriptModule {
        fun loaders(): List<ApplicationResourceLoader<*>>
}