package playwrigkt.skript.user

import playwrigkt.skript.Skript
import playwrigkt.skript.produktion.Produktion
import playwrigkt.skript.queue.QueueMessage
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.venue.QueueVenue

val userCreatedAddress = "user.updated"
val userLoginAddress = "user.login"

fun userLoginProduktion(venue: QueueVenue, stageManager: ApplicationStageManager, skript: Skript<QueueMessage, Unit, ApplicationTroupe<Unit>>): AsyncResult<out Produktion> {
    return venue.produktion(skript, stageManager, userLoginAddress)
}

fun userCreateProduktion(venue: QueueVenue, stageManager: ApplicationStageManager, skript: Skript<QueueMessage, Unit, ApplicationTroupe<Unit>>): AsyncResult<out Produktion> {
    return venue.produktion(skript, stageManager, userCreatedAddress)
}