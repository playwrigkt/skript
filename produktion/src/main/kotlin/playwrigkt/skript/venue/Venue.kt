package playwrigkt.skript.venue

import playwrigkt.skript.Skript
import playwrigkt.skript.produktion.Performance
import playwrigkt.skript.produktion.Production
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager

interface Venue<Rule, Beginning> { //This is a stage
    fun <O, Troupe> sink(skript: Skript<Beginning, O, Troupe>,
                              stageManager: StageManager<Troupe>,
                              rule: Rule): AsyncResult<Production>

    fun <I, O, Troupe> performance(skript: Skript<I, O, Troupe>,
                             stageManager: StageManager<Troupe>): Performance<I, O, Troupe> = Performance(skript, stageManager)
}
