package playwright.skript.stage

import playwright.skript.performer.SerializePerformer

interface SerializeStage<E: SerializePerformer> {
    fun getSerializePerformer(): E
}