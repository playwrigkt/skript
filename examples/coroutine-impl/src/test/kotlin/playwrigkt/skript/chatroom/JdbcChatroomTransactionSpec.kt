package playwrigkt.skript.chatroom

import io.kotlintest.Description
import io.kotlintest.Spec
import playwrigkt.skript.amqp.AmqpManager

class JdbcChatroomTransactionSpec: ChatroomTransactionsSpec() {
    override val sourceConfigFileName: String = "coroutine-application.json"

    override fun beforeSpec(description: Description, spec: Spec) {
        AmqpManager.cleanConnection(AmqpManager.connectionFactory()).close()
        super.beforeSpec(description, spec)
    }

    override fun afterSpec(description: Description, spec: Spec) {
        super.afterSpec(description, spec)
        AmqpManager.cleanConnection(AmqpManager.connectionFactory()).close()
    }
}