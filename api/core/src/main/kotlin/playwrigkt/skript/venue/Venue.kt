package playwrigkt.skript.venue

import org.slf4j.LoggerFactory
import playwrigkt.skript.Skript
import playwrigkt.skript.application.ApplicationResource
import playwrigkt.skript.produktion.Performance
import playwrigkt.skript.produktion.Produktion
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager

/**
 * A Venue manages an input resources and various handlers for that input.
 *
 * Some examples are http servers, queue consumers, text input
 */
abstract class Venue<Rule, Beginning, Ending>: ApplicationResource {
    val log = LoggerFactory.getLogger(this::class.java)

    init {
        log.info("Starting venue $this")
    }

    fun <Troupe> produktion(skript: Skript<Beginning, Ending, Troupe>,
                            stageManager: StageManager<Troupe>,
                            rule: Rule): AsyncResult<out Produktion> =
            createProduktion(skript, stageManager, rule)
                    .onSuccess { log.info("...Added new produktion...: $it") }
                    .onFailure { log.error("...Failed to create produktion...", it) }


    protected abstract fun <Troupe> createProduktion(skript: Skript<Beginning, Ending, Troupe>,
                                                     stageManager: StageManager<Troupe>,
                                                     rule: Rule): AsyncResult<out Produktion>

    fun <I, O, Troupe> performance(skript: Skript<I, O, Troupe>,
                                   stageManager: StageManager<Troupe>): Performance<I, O, Troupe> = Performance(skript, stageManager)
}