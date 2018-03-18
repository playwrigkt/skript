package playwright.skript.ex

import playwright.skript.Skript

fun <I, O, O2, Stage: SubStage, SubStage> Skript<I, O, Stage>.andThen(skript: Skript<O, O2, SubStage>): Skript<I, O2, Stage> {
    return this.flatMap(Skript.Wrapped<O, O2, Stage, SubStage>(skript))
}