package playwrigkt.skript.venue

import playwrigkt.skript.Skript
import playwrigkt.skript.produktion.Performance
import playwrigkt.skript.produktion.Produktion
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager

interface Venue<Rule, Beginning, Ending> { //This is a stage
    fun <Troupe> produktion(skript: Skript<Beginning, Ending, Troupe>,
                               stageManager: StageManager<Troupe>,
                               rule: Rule): AsyncResult<out Produktion>

    fun <I, O, Troupe> performance(skript: Skript<I, O, Troupe>,
                             stageManager: StageManager<Troupe>): Performance<I, O, Troupe> = Performance(skript, stageManager)
}
