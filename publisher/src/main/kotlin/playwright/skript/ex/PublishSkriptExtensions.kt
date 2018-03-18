package playwright.skript.ex

import playwright.skript.Skript
import playwright.skript.performer.PublishCommand
import playwright.skript.publish.PublishSkript
import playwright.skript.stage.PublishStage

fun <I, O, Stage: PublishStage> Skript<I, O, Stage>.publish(mapping: (O) -> PublishCommand.Publish) =
        this.andThen(PublishSkript.publish(mapping))