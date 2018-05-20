package playwrigkt.skript.stagemanager

import playwrigkt.skript.config.ConfigValue
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.ConfigTroupe
import playwrigkt.skript.troupe.SyncConfigTroupe

class SyncConfigStageManager(val configValue: ConfigValue): StageManager<ConfigTroupe> {
    override fun hireTroupe(): ConfigTroupe = SyncConfigTroupe(configValue)

    override fun tearDown(): AsyncResult<Unit> = AsyncResult.succeeded(Unit)
}