package playwrigkt.skript.user

import playwrigkt.skript.common.ApplicationStage
import playwrigkt.skript.common.ApplicationVenue
import playwrigkt.skript.consumer.alpha.ConsumedMessage

val userCreatedAddress = "user.updated"
val userLoginAddress = "user.login"

fun userLoginConsumer(consumerPerformer: playwrigkt.skript.consumer.alpha.ConsumerStage<String, ConsumedMessage>, applicationVenue: ApplicationVenue): playwrigkt.skript.consumer.alpha.ConsumerPerformer<ApplicationStage<Unit>, ConsumedMessage> {
    return consumerPerformer.buildPerformer(userLoginAddress, applicationVenue)
}

fun userCreateConsumer(consumerPerformer: playwrigkt.skript.consumer.alpha.ConsumerStage<String, ConsumedMessage>, applicationVenue: ApplicationVenue): playwrigkt.skript.consumer.alpha.ConsumerPerformer<ApplicationStage<Unit>, ConsumedMessage> {
    return consumerPerformer.buildPerformer(userCreatedAddress, applicationVenue)
}