package playwrigkt.skript.stagemanager

import io.vertx.core.Vertx
import playwrigkt.skript.troupe.QueuePublishTroupe
import playwrigkt.skript.troupe.VertxPublishTroupe

data class VertxPublishStageManager(val vertx: Vertx): StageManager<QueuePublishTroupe> {
    override fun hireTroupe(): QueuePublishTroupe = VertxPublishTroupe(vertx)
}