package playwrigkt.skript.ex

import playwrigkt.skript.Skript
import playwrigkt.skript.performer.PublishCommand
import playwrigkt.skript.publish.PublishSkript
import playwrigkt.skript.stage.PublishStage

fun <I, O, Stage> Skript<I, O, Stage>.publish(mapping: (O) -> PublishCommand.Publish) where Stage: PublishStage =
        this.andThen(PublishSkript.publish(mapping))