package playwrigkt.skript.chatroom

import io.kotlintest.Description
import io.kotlintest.Spec
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import playwrigkt.skript.Async
import playwrigkt.skript.ExampleApplication
import playwrigkt.skript.result.VertxResult
import playwrigkt.skript.vertx.createApplication
import kotlin.math.floor

class VertxChatroomTransactionSpec: ChatroomTransactionsSpec() {

    companion object {
        val port = floor((Math.random() * 8000)).toInt() + 2000

        val application by lazy { Async.awaitSucceededFuture(createApplication(port))!! }
    }

    override fun application(): ExampleApplication = application
}