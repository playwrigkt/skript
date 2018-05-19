package playwrigkt.skript.chatroom

import io.kotlintest.Description
import io.kotlintest.Spec
import playwrigkt.skript.amqp.AmqpManager
import playwrigkt.skript.application.CoroutineAmqpVenueLoader

class JdbcChatroomTransactionSpec: ChatroomTransactionsSpec() {
    override val sourceConfigFileName: String = "coroutine-application.json"
    override val queueVenueName: String = CoroutineAmqpVenueLoader.name()

    override fun beforeSpec(description: Description, spec: Spec) {
        super.beforeSpec(description, spec)
        AmqpManager.cleanConnection(AmqpManager.connectionFactory()).close()
    }

    override fun afterSpec(description: Description, spec: Spec) {
        super.afterSpec(description, spec)
        AmqpManager.cleanConnection(AmqpManager.connectionFactory()).close()
    }
}