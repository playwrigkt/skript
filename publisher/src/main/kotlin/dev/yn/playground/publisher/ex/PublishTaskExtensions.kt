package dev.yn.playground.publisher.ex

import dev.yn.playground.publisher.PublishCommand
import dev.yn.playground.publisher.PublishTask
import dev.yn.playground.publisher.PublishTaskContext
import dev.yn.playground.task.Task

fun <I, O, C: PublishTaskContext<*>> Task<I, O, C>.publish(mapping: (O) -> PublishCommand.Publish) =
        this.andThen(PublishTask.publish(mapping))