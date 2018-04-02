package playwrigkt.skript.venue

import playwrigkt.skript.result.AsyncResult

interface StageManager<out Troupe> {
    fun hireTroupe(): Troupe
}