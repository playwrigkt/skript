package playwrigkt.skript.ex

import playwrigkt.skript.config.ConfigValue
import java.math.BigDecimal
import java.math.BigInteger

fun Int.configValue() = ConfigValue.Number(BigInteger.valueOf(this.toLong()))
fun Long.configValue() = ConfigValue.Number(BigInteger.valueOf(this))
fun Double.configValue() = ConfigValue.Decimal(BigDecimal.valueOf(this))
fun BigInteger.configValue() = ConfigValue.Number(this)
fun BigDecimal.configValue() = ConfigValue.Decimal(this)
fun String.configValue() = ConfigValue.Text(this)
fun Boolean.configValue() = ConfigValue.Bool(this)
fun List<ConfigValue>.configValue() = ConfigValue.Collection.Array(this)
fun Map<String, ConfigValue>.configValue() = ConfigValue.Collection.Object(this)