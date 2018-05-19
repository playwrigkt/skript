package playwrigkt.skript.application

import playwrigkt.skript.Skript
import playwrigkt.skript.config.ConfigValue
import playwrigkt.skript.queue.QueueMessage

object QueueProduktionManagerLoader: ProduktionManagerLoader<String, QueueMessage, Unit, Any>() {
    override val parseRuleConfig: Skript<ConfigValue, String, SkriptApplicationLoader> =
            Skript.identity<ConfigValue, SkriptApplicationLoader>()
                    .mapTry { it.text() }
                    .map { it.value }
}