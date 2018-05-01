package playwrigkt.skript.config

import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import java.math.BigDecimal
import java.math.BigInteger

sealed class ConfigValue {
    object Empty: ConfigValue()
    sealed class Collection: ConfigValue() {
        data class Object(val values: Map<String, ConfigValue>): ConfigValue()
        data class Array(val list: List<ConfigValue>): ConfigValue()
    }
    data class Text(val value: String): ConfigValue()
    data class Number(val value: BigInteger): ConfigValue()
    data class Decimal(val value: BigDecimal): ConfigValue()
}

interface ConfigPerformer {
    fun getNumber(config: String): AsyncResult<ConfigValue.Number>
    fun getDecimal(config: String): AsyncResult<ConfigValue.Decimal>
    fun getText(config: String): AsyncResult<ConfigValue.Text>
    fun getConfigValue(config: String): AsyncResult<ConfigValue>
}

interface ConfigTroupe {
    fun getConfigPerformer(): AsyncResult<out ConfigPerformer>
}

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