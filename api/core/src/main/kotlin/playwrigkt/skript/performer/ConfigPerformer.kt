package playwrigkt.skript.performer

import playwrigkt.skript.config.ConfigValue
import playwrigkt.skript.result.AsyncResult

interface ConfigPerformer {
    fun getNumber(config: String): AsyncResult<ConfigValue.Number>
    fun getDecimal(config: String): AsyncResult<ConfigValue.Decimal>
    fun getText(config: String): AsyncResult<ConfigValue.Text>
    fun getConfigValue(config: String): AsyncResult<ConfigValue>
}