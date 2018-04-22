package playwrigkt.skript.venue

import org.funktionale.tries.Try
import org.slf4j.LoggerFactory
import playwrigkt.skript.Skript
import playwrigkt.skript.ex.lift
import playwrigkt.skript.ex.toAsyncResult
import playwrigkt.skript.produktion.Performance
import playwrigkt.skript.produktion.Produktion
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager
import java.util.concurrent.LinkedBlockingQueue

/**
 * A Venue manages an input resources and various handlers for that input.
 *
 * Some examples are http servers, queue consumers, text input
 */
abstract class Venue<Rule, Beginning, Ending> {
    val log = LoggerFactory.getLogger(this::class.java)
    private val produktions = LinkedBlockingQueue<Produktion>()

    init {
        log.info("Starting venue $this")
    }

    fun <Troupe> produktion(skript: Skript<Beginning, Ending, Troupe>,
                            stageManager: StageManager<Troupe>,
                            rule: Rule): AsyncResult<out Produktion> =
            createProduktion(skript, stageManager, rule)
                    .map {
                        produktions.add(it)
                        it
                    }
                    .onSuccess { log.info("...Added new produktion...: ${it}") }
                    .onFailure { log.error("...Failed to create produktion...", it) }

    protected abstract fun <Troupe> createProduktion(skript: Skript<Beginning, Ending, Troupe>,
                                                     stageManager: StageManager<Troupe>,
                                                     rule: Rule): AsyncResult<out Produktion>

    fun <I, O, Troupe> performance(skript: Skript<I, O, Troupe>,
                                   stageManager: StageManager<Troupe>): Performance<I, O, Troupe> = Performance(skript, stageManager)

    fun teardown(): AsyncResult<Unit> =
            stopAllProduktions()
                    .flatMap { this
                            .stop()
                            .onSuccess { log.info("...Stopped venue...: ${this}") }
                    }
                    .onFailure { log.error("...Failed to stop venue...: ${this}", it) }

    fun stopProduktion(produktion: Produktion): AsyncResult<Unit> =
            Try {
                produktions.remove(produktion)
                produktion
            }
                    .toAsyncResult()
                    .onFailure { log.error("...Could not remove produktion...: $produktion", it) }
                    .recover { AsyncResult.succeeded(produktion) }
                    .flatMap { it.stop() }
                    .onSuccess { log.info("...Stopped produktion...: ${produktion}") }
                    .onFailure { log.error("...Failed to stop produktion...: ${produktion}", it) }

    protected abstract fun stop(): AsyncResult<Unit>

    private fun stopAllProduktions(): AsyncResult<List<Unit>> {
        val l = mutableListOf<AsyncResult<Unit>>()
        while(produktions.isNotEmpty()) {
            l.add(Try { produktions.remove() }
                    .toAsyncResult()
                    .onFailure { log.error("...Could not poll produktion...", it) }
                    .flatMap { produktion ->
                        produktion.stop()
                                .onSuccess { log.info("...Stopped produktion...: ${produktion}") }
                                .onFailure { log.error("...Failed to stop produktion...: ${produktion}", it) }
                    })

        }
        return l.lift()
    }
}