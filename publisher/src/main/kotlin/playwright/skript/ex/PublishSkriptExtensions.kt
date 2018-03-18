package playwright.skript.ex

import playwright.skript.Skript
import playwright.skript.performer.PublishCommand
import playwright.skript.publish.PublishSkript
import playwright.skript.stage.PublishCast

fun <I, O, Stage> Skript<I, O, Stage>.publish(mapping: (O) -> PublishCommand.Publish) where Stage: PublishCast =
        this.andThen(PublishSkript.publish(mapping))