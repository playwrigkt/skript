package playwrigkt.skript.ex

import playwrigkt.skript.Skript
import playwrigkt.skript.publish.PublishSkript
import playwrigkt.skript.troupe.PublishTroupe

fun <I, O, Troupe, Message> Skript<I, O, Troupe>.publish(mapping: (O) -> Message) where Troupe: PublishTroupe<Message> =
        this.andThen(PublishSkript.publish(mapping))