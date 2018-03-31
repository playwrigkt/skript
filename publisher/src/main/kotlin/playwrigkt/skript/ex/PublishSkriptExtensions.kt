package playwrigkt.skript.ex

import org.funktionale.tries.Try
import playwrigkt.skript.Skript
import playwrigkt.skript.publish.PublishSkript
import playwrigkt.skript.troupe.PublishTroupe

fun <I, Message, Troupe> Skript<I, Message, Troupe>.publish(): Skript<I, Unit, Troupe> where Troupe: PublishTroupe<Message> = this.andThen(PublishSkript.publish())

fun <I, O, Troupe, Message> Skript<I, O, Troupe>.publish(mapping: (O) -> Message): Skript<I, O, Troupe> where Troupe: PublishTroupe<Message> =
        this
                .split(Skript.identity<O, Troupe>().map(mapping).publish())
                .join { input, _ -> Try.Success(input) }