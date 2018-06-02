package playwrigkt.skript.troupe

import playwrigkt.skript.config.ConfigValue
import playwrigkt.skript.performer.ConfigPerformer
import playwrigkt.skript.performer.SyncConfigPerformer
import playwrigkt.skript.result.AsyncResult

class SyncConfigTroupe(val configValue: ConfigValue): ConfigTroupe {
    private val configPerformer = SyncConfigPerformer(configValue)

    override fun getConfigPerformer(): AsyncResult<out ConfigPerformer> = AsyncResult.succeeded(configPerformer)
}