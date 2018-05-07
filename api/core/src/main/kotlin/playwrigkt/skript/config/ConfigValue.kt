package playwrigkt.skript.config

import java.math.BigDecimal
import java.math.BigInteger

sealed class ConfigValue {
    sealed class Empty: ConfigValue() {
        object Undefined: Empty()
        object Null: Empty()
    }
    sealed class Collection: ConfigValue() {
        data class Object(val values: Map<String, ConfigValue>): ConfigValue()
        data class Array(val list: List<ConfigValue>): ConfigValue()
    }
    data class Text(val value: String): ConfigValue()
    data class Number(val value: BigInteger): ConfigValue()
    data class Decimal(val value: BigDecimal): ConfigValue()
    data class Bool(val value: Boolean): ConfigValue()
}