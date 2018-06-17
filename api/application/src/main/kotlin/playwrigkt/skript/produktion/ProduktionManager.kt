package playwrigkt.skript.produktion

import org.slf4j.LoggerFactory
import playwrigkt.skript.Skript
import playwrigkt.skript.application.ApplicationResource
import playwrigkt.skript.ex.*
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.result.CompletableResult
import playwrigkt.skript.result.LightweightSynchronized
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.venue.Venue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock


data class ProduktionManagerConfig<Rule>(val produktions: List<ProduktionConfig<Rule>>)

data class ProduktionConfig<Rule>(val className: String,
                                  val mappings: Map<String, Rule>)


data class ProduktionsManager<Rule, Beginning, End, Troupe>(val venue: Venue<Rule, Beginning, End>,
                                                            val stageManager: StageManager<Troupe>,
                                                            val rulesAndSkripts: Map<Rule, Skript<Beginning, End, Troupe>>): ApplicationResource {
    val produktionManagers: AsyncResult<List<ProduktionManager<Rule, Beginning, End, Troupe>>> = rulesAndSkripts
            .map {
                ProduktionManager.of(venue, stageManager, it.key, it.value)
            }
            .lift()

    override fun tearDown(): AsyncResult<Unit> =
            produktionManagers
                    .flatMap { it
                            .map { it.stop() }
                            .lift()
                    }
                    .map { Unit }
}

/**
 * TODO harden bouncing restart. i.e. backoff delay,
 */
data class ProduktionManager<Rule, Beginning, End, Troupe>(val venue: Venue<Rule, Beginning, End>,
                                                           val stageManager: StageManager<Troupe>,
                                                           val rule: Rule,
                                                           val skript: Skript<Beginning, End, Troupe>): LightweightSynchronized {
    val log = LoggerFactory.getLogger(this::class.java)

    override val lock: ReentrantLock = ReentrantLock()
    @Volatile private var restart = AtomicBoolean(true)
    private val produktion = AtomicReference<Produktion>()
    private val startTime = AtomicLong(System.currentTimeMillis())
    private val recentTimes = LinkedBlockingQueue<Long>()

    private val result = CompletableResult<Unit>()
    companion object {
        fun <Rule, Beginning, End, Troupe> of(
                venue: Venue<Rule, Beginning, End>,
                stageManager: StageManager<Troupe>,
                rule: Rule,
                skript: Skript<Beginning, End, Troupe>): AsyncResult<ProduktionManager<Rule, Beginning, End, Troupe>> {
            val manager = ProduktionManager(venue, stageManager, rule, skript)
            return manager.startProduktion()
                    .map { manager }
        }

    }

    fun stop(): AsyncResult<Unit> {
        log.info("stopping produktion.. $rule")
        if(this.restart.getAndSet(false)){
            this.produktion.get()
                    .stop()
                    .addHandler(result.completionHandler())
        }
        return result
    }

    private fun applyRestart(result: AsyncResult<out Produktion>) = result
            .flatMap {
                lock {
                    log.info("created produktion.. $rule")
                    this.produktion.set(it)
                }
                it.result()
            }
            .addHandler {
                log.info("produktion has stopped.. $it")
                lock {
                    if(restart.get()) {
                        val timeAlive = System.currentTimeMillis() - startTime.get()
                        if(recentTimes.size > 10) {
                            recentTimes.remove()
                        }
                        recentTimes.add(timeAlive)
                        val average = recentTimes.average()
                        if(average < 1000) {
                            log.info("Produktion has bounced at an average rate of $average ms, waiting for a second before restarting")
                            Thread.sleep(1000)
                        }
                        log.info("restarting.. $rule")
                        this.startProduktion()
                    }
                }
            }

    private fun startProduktion(): AsyncResult<out Produktion> {
        startTime.set(System.currentTimeMillis())
        return venue.produktion(skript, stageManager, rule)
                .let {
                    applyRestart(it)
                    it
                }
    }
}
