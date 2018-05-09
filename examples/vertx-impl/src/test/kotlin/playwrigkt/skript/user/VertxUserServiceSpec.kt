package playwrigkt.skript.user

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

class VertxUserServiceSpec: UserServiceSpec() {
    companion object {
        val port = floor((Math.random() * 8000)).toInt() + 2000

        val application by lazy { Async.awaitSucceededFuture(createApplication(port))!! }

        val userHttpClient by lazy { UserHttpClient(port) }
    }

    override fun userHttpClient(): UserHttpClient = userHttpClient
    override fun application(): ExampleApplication = application
}