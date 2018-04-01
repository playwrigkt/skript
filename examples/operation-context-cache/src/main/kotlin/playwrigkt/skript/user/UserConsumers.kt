package playwrigkt.skript.user

import playwright.skript.consumer.alpha.QueueMessage
import playwright.skript.consumer.alpha.QueueVenue
import playwrigkt.skript.Skript
import playwrigkt.skript.common.ApplicationStageManager
import playwrigkt.skript.common.ApplicationTroupe
import playwrigkt.skript.consumer.alpha.Production
import playwrigkt.skript.result.AsyncResult

val userCreatedAddress = "user.updated"
val userLoginAddress = "user.login"

fun <O> userLoginConsumer(consumerPerformer: QueueVenue, applicationVenue: ApplicationStageManager, skript: Skript<QueueMessage, O, ApplicationTroupe<Unit>>): AsyncResult<Production> {
    return consumerPerformer.sink(skript, applicationVenue, userLoginAddress)
}

fun <O> userCreateConsumer(consumerPerformer: QueueVenue, applicationVenue: ApplicationStageManager, skript: Skript<QueueMessage, O, ApplicationTroupe<Unit>>): AsyncResult<Production> {
    return consumerPerformer.sink(skript, applicationVenue, userCreatedAddress)
}