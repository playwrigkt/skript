package playwrigkt.skript.produktion

import playwrigkt.skript.Skript
import playwrigkt.skript.http.HttpRequest
import playwrigkt.skript.http.HttpResponse
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager


class HttpProduction<STAGE>(
        val stageManager: StageManager<STAGE>,
        val skript: Skript<HttpRequest, HttpResponse, STAGE>): Production {
    override fun isRunning(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stop(): AsyncResult<Unit> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun result(): AsyncResult<Unit> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}