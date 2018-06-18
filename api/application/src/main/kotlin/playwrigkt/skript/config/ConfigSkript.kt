package playwrigkt.skript.config

import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.ConfigTroupe


sealed class ConfigSkript<O: ConfigValue>: Skript<String, O, ConfigTroupe> {
    object Text: ConfigSkript<ConfigValue.Text>() {
        override fun run(i: String, troupe: ConfigTroupe): AsyncResult<ConfigValue.Text> =
            troupe.getConfigPerformer()
                    .flatMap { it.getText(i) }
        }

    object Number: ConfigSkript<ConfigValue.Number>() {
        override fun run(i: String, troupe: ConfigTroupe): AsyncResult<ConfigValue.Number> =
            troupe.getConfigPerformer()
                    .flatMap { it.getNumber(i) }
    }

    object Decimal: ConfigSkript<ConfigValue.Decimal>() {
        override fun run(i: String, troupe: ConfigTroupe): AsyncResult<ConfigValue.Decimal> =
                troupe.getConfigPerformer()
                        .flatMap { it.getDecimal(i) }
    }

    object Config: ConfigSkript<ConfigValue>() {
        override fun run(i: String, troupe: ConfigTroupe): AsyncResult<ConfigValue> =
                troupe.getConfigPerformer()
                        .flatMap { it.getConfigValue(i) }
    }
}