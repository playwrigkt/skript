package playwrigkt.skript.ex

import playwrigkt.skript.Skript
import playwrigkt.skript.queue.QueueMessage
import playwrigkt.skript.queue.QueuePublishSkript
import playwrigkt.skript.troupe.QueuePublishTroupe

fun <I, Troupe> Skript<I, QueueMessage, Troupe>.publish(): Skript<I, Unit, Troupe> where Troupe: QueuePublishTroupe = this.andThen(QueuePublishSkript.publish())

fun <I, O, Troupe> Skript<I, O, Troupe>.publish(mapping: (O) -> QueueMessage): Skript<I, O, Troupe> where Troupe: QueuePublishTroupe =
        this
                .split(Skript.identity<O, Troupe>()
                        .map{ mapping(it) }
                        .publish())
                .join { input, _ -> input }