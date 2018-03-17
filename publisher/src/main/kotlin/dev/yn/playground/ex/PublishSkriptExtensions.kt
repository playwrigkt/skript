package dev.yn.playground.ex

import dev.yn.playground.Skript
import dev.yn.playground.context.PublishSkriptContext
import dev.yn.playground.publisher.PublishCommand
import dev.yn.playground.publisher.PublishSkript

fun <I, O, C: PublishSkriptContext<*>> Skript<I, O, C>.publish(mapping: (O) -> PublishCommand.Publish) =
        this.andThen(PublishSkript.publish(mapping))