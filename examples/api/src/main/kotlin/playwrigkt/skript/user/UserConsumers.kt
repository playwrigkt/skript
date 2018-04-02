package playwrigkt.skript.user

import playwright.skript.queue.QueueMessage
import playwright.skript.venue.QueueVenue
import playwrigkt.skript.Skript
import playwrigkt.skript.common.ApplicationStageManager
import playwrigkt.skript.common.ApplicationTroupe
import playwrigkt.skript.produktion.Production
import playwrigkt.skript.result.AsyncResult

val userCreatedAddress = "user.updated"
val userLoginAddress = "user.login"

fun <O> userLoginConsumer(consumerPerformer: QueueVenue, applicationVenue: ApplicationStageManager, skript: Skript<QueueMessage, O, ApplicationTroupe>): AsyncResult<Production> {
    return consumerPerformer.sink(
            skript,
            applicationVenue,
            userLoginAddress
    )
}

fun <O> userCreateConsumer(consumerPerformer: QueueVenue, applicationVenue: ApplicationStageManager, skript: Skript<QueueMessage, O, ApplicationTroupe>): AsyncResult<Production> {
    return consumerPerformer.sink(
            skript,
            applicationVenue,
            userCreatedAddress
    )
}