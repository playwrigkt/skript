package playwright.skript.publish

import playwright.skript.consumer.alpha.QueueMessage
import playwrigkt.skript.publish.PublishSkript

typealias QueuePublishSkript<I> = PublishSkript<I, QueueMessage>