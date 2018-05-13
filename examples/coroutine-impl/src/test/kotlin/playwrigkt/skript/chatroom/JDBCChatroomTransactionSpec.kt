package playwrigkt.skript.chatroom

import io.kotlintest.Description
import io.kotlintest.Spec
import playwrigkt.skript.amqp.AMQPManager

class JDBCChatroomTransactionSpec: ChatroomTransactionsSpec() {
    override val sourceConfigFileName: String = "coroutine-application.json"

    override fun beforeSpec(description: Description, spec: Spec) {
        super.beforeSpec(description, spec)
        AMQPManager.cleanConnection(AMQPManager.connectionFactory()).close()
    }

    override fun afterSpec(description: Description, spec: Spec) {
        super.afterSpec(description, spec)
        AMQPManager.cleanConnection(AMQPManager.connectionFactory()).close()
    }
}