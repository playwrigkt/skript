package playwrigkt.skript.chatroom

import playwrigkt.skript.application.VertxVenueLoader

class VertxChatroomTransactionSpec: ChatroomTransactionsSpec() {
    override val sourceConfigFileName: String = "vertx-application.json"
    override val queueVenueName: String = VertxVenueLoader.name()
}