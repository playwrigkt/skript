package playwrigkt.skript.user

import playwrigkt.skript.Skript
import playwrigkt.skript.ex.deserialize
import playwrigkt.skript.queue.QueueMessage
import playwrigkt.skript.troupe.ApplicationTroupe
import playwrigkt.skript.user.models.UserProfile
import playwrigkt.skript.user.models.UserSession
import java.util.concurrent.LinkedBlockingQueue

object UserQueueSkripts {
    val processedLoginEvents = LinkedBlockingQueue<UserSession>()
    val processedCreateEvents = LinkedBlockingQueue<UserProfile>()

    val processLoginEvent = Skript.identity<QueueMessage, ApplicationTroupe>()
            .map { it.body }
            .deserialize(UserSession::class.java)
            .map(processedLoginEvents::add)
            .map { Unit }

    val processCreateEvent = Skript.identity<QueueMessage, ApplicationTroupe>()
            .map { it.body }
            .deserialize(UserProfile::class.java)
            .map(processedCreateEvents::add)
            .map { Unit }
}