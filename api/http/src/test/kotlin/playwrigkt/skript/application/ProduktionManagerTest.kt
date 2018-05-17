package playwrigkt.skript.application

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.http.Http
import playwrigkt.skript.http.server.HttpServer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ExampleSkriptConfig {
    val skript1 = Skript.identity<String, Unit>()
            .map { it.toInt() }
            .map { it * 2 }

    val getSkript2 = Skript.identity<String, DateTimeFormatter>()
            .mapWithTroupe { string, troupe -> LocalDateTime.parse(string, troupe) }

}
class ProduktionManagerTest: StringSpec() {
    init {
        "get the things off of a class" {
            val skript1Rule =HttpServer.Endpoint("/pathy", emptyMap(), Http.Method.Get)
            val skript2Rule =HttpServer.Endpoint("/alsoo", emptyMap(), Http.Method.Post)
            val produktionConfig = ProduktionConfig(
                    "playwrigkt.skript.application.ExampleSkriptConfig",
                    mapOf("skript1" to skript1Rule,
                            "getSkript2" to skript2Rule))

            val result = getSkriptsFromClass.run(produktionConfig, Unit)
            result.isSuccess() shouldBe true
            result.result() shouldBe mapOf(
                    skript1Rule to ExampleSkriptConfig.skript1,
                    skript2Rule to ExampleSkriptConfig.getSkript2)
        }
    }
}
