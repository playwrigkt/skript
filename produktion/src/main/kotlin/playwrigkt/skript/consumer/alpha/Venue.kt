package playwrigkt.skript.consumer.alpha

import playwrigkt.skript.Skript
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.venue.StageManager

interface Venue<Rule, Beginning> { //This is a stage
    fun <O, Troupe> sink(skript: Skript<Beginning, O, Troupe>,
                              stageManager: StageManager<Troupe>,
                              rule: Rule): AsyncResult<Production>

    fun <I, O, Troupe> performance(skript: Skript<I, O, Troupe>,
                             stageManager: StageManager<Troupe>): Performance<I, O, Troupe> = Performance(skript, stageManager)
}
