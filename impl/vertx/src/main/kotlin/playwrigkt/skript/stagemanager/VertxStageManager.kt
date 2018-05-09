package playwrigkt.skript.stagemanager

import io.vertx.core.Vertx
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.vertx.ex.vertxHandler

class VertxStageManager(val vertx: Vertx): StageManager<Vertx> {
    override fun hireTroupe(): Vertx = vertx

    override fun tearDown(): AsyncResult<Unit> {
        val result = CompletableResult<Unit>()
        vertx.close(result.vertxHandler())
        return result
    }
}