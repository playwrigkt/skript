package playwrigkt.skript.ex

import playwrigkt.skript.Skript
import playwrigkt.skript.performer.PublishCommand
import playwrigkt.skript.publish.PublishSkript
import playwrigkt.skript.stage.PublishCast

fun <I, O, Stage> Skript<I, O, Stage>.publish(mapping: (O) -> PublishCommand.Publish) where Stage: PublishCast =
        this.andThen(PublishSkript.publish(mapping))