package playwright.skript.user

import playwright.skript.common.ApplicationStage
import playwright.skript.common.ApplicationVenue

val userCreatedAddress = "user.updated"
val userLoginAddress = "user.login"

fun userLoginConsumer(consumerPerformer: playwright.skript.consumer.alpha.ConsumerStage, applicationVenue: ApplicationVenue): playwright.skript.consumer.alpha.ConsumerPerformer<ApplicationStage<Unit>> {
    return consumerPerformer.buildPerformer(userLoginAddress, applicationVenue)
}

fun userCreateConsumer(consumerPerformer: playwright.skript.consumer.alpha.ConsumerStage, applicationVenue: ApplicationVenue): playwright.skript.consumer.alpha.ConsumerPerformer<ApplicationStage<Unit>> {
    return consumerPerformer.buildPerformer(userCreatedAddress, applicationVenue)
}