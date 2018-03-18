package playwright.skript.performer

import playwright.skript.result.AsyncResult

sealed class PublishCommand {
    data class Publish(val target: String, val body: ByteArray): PublishCommand()
}
interface PublishPerformer {
    fun publish(command: PublishCommand.Publish): AsyncResult<Unit>
}