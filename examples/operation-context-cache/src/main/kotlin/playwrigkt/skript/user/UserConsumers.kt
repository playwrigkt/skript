package playwrigkt.skript.user

import playwright.skript.consumer.alpha.QueueConsumerProduction
import playwright.skript.consumer.alpha.QueueConsumerTroupe
import playwrigkt.skript.common.ApplicationStage
import playwrigkt.skript.common.ApplicationVenue

val userCreatedAddress = "user.updated"
val userLoginAddress = "user.login"

fun userLoginConsumer(consumerPerformer: QueueConsumerTroupe, applicationVenue: ApplicationVenue): QueueConsumerProduction<ApplicationStage<Unit>> {
    return consumerPerformer.production(userLoginAddress, applicationVenue)
}

fun userCreateConsumer(consumerPerformer: QueueConsumerTroupe, applicationVenue: ApplicationVenue): QueueConsumerProduction<ApplicationStage<Unit>> {
    return consumerPerformer.production(userCreatedAddress, applicationVenue)
}