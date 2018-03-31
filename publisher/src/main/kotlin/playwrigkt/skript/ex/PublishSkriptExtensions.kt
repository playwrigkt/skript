package playwrigkt.skript.ex

import playwrigkt.skript.Skript
import playwrigkt.skript.publish.PublishSkript
import playwrigkt.skript.stage.PublishStage

fun <I, O, Stage, Message> Skript<I, O, Stage>.publish(mapping: (O) -> Message) where Stage: PublishStage<Message> =
        this.andThen(PublishSkript.publish(mapping))