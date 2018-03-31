package playwrigkt.skript.stagemanager

interface StageManager<out Troupe> {
    fun hireTroupe(): Troupe
}