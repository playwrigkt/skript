package playwrigkt.skript.performer

import playwrigkt.skript.config.ConfigValue
import playwrigkt.skript.ex.toAsyncResult
import playwrigkt.skript.result.AsyncResult



class SyncConfigPerformer(val configValue: ConfigValue): ConfigPerformer {
    private val delimiter = "."

    override fun getNumber(config: String): AsyncResult<ConfigValue.Number> =
            configValue.applyPath(config, delimiter)
                    .flatMap { it.number() }
                    .toAsyncResult()

    override fun getDecimal(config: String): AsyncResult<ConfigValue.Decimal> =
            configValue.applyPath(config, delimiter)
                    .flatMap { it.decimal() }
                    .toAsyncResult()


    override fun getText(config: String): AsyncResult<ConfigValue.Text> =
            configValue.applyPath(config, delimiter)
                    .flatMap { it.text() }
                    .toAsyncResult()


    override fun getConfigValue(config: String): AsyncResult<ConfigValue> =
            configValue.applyPath(config, delimiter)
                    .toAsyncResult()

    override fun getBool(config: String): AsyncResult<ConfigValue.Bool> =
            configValue.applyPath(config, delimiter)
                    .flatMap { it.bool() }
                    .toAsyncResult()

    override fun getObject(config: String): AsyncResult<ConfigValue.Collection.Object> =
            configValue.applyPath(config, delimiter)
                    .flatMap { it.objekt() }
                    .toAsyncResult()

    override fun getArray(config: String): AsyncResult<ConfigValue.Collection.Array> =
            configValue.applyPath(config, delimiter)
                    .flatMap { it.array() }
                    .toAsyncResult()
}