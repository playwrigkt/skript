package playwright.skript.stage

import playwright.skript.performer.SerializePerformer

interface SerializeCast {
    fun getSerializePerformer(): SerializePerformer
}