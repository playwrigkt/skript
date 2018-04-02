package playwrigkt.skript.user

import playwrigkt.skript.queue.QueueMessage
import playwrigkt.skript.venue.QueueVenue
import playwrigkt.skript.Skript
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.produktion.Production
import playwrigkt.skript.result.AsyncResult

val userCreatedAddress = "user.updated"
val userLoginAddress = "user.login"

fun <O> userLoginConsumer(consumerPerformer: QueueVenue, applicationVenue: ApplicationStageManager, skript: Skript<QueueMessage, O, ApplicationTroupe<Unit>>): AsyncResult<Production> {
    return consumerPerformer.sink(skript, applicationVenue, userLoginAddress)
}

fun <O> userCreateConsumer(consumerPerformer: QueueVenue, applicationVenue: ApplicationStageManager, skript: Skript<QueueMessage, O, ApplicationTroupe<Unit>>): AsyncResult<Production> {
    return consumerPerformer.sink(skript, applicationVenue, userCreatedAddress)
}