package playwrigkt.skript.user

import playwrigkt.skript.Skript
import playwrigkt.skript.queue.QueueMessage
import playwrigkt.skript.stagemanager.ApplicationStageManager
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.venue.QueueVenue

val userCreatedAddress = "user.updated"
val userLoginAddress = "user.login"

fun userLoginQueueProduktion(skript: Skript<QueueMessage, Unit, ApplicationTroupe>, queueVenue: QueueVenue, stageManager: ApplicationStageManager) =
        queueVenue.produktion(
                skript,
                stageManager,
                userLoginAddress)

fun userCreateQueueProduktion(skript: Skript<QueueMessage, Unit, ApplicationTroupe>, queueVenue: QueueVenue, stageManager: ApplicationStageManager) =
        queueVenue.produktion(
                skript,
                stageManager,
                userCreatedAddress)