package playwright.skript.stage

import playwright.skript.performer.SerializePerformer

interface SerializeStage {
    fun getSerializePerformer(): SerializePerformer
}