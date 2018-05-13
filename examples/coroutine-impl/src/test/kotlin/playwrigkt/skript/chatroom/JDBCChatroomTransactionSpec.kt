package playwrigkt.skript.chatroom

import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import io.kotlintest.Description
import io.kotlintest.Spec
import playwrigkt.skript.Async
import playwrigkt.skript.ExampleApplication
import playwrigkt.skript.coroutine.AMQPManager
import playwrigkt.skript.coroutine.startApplication
import kotlin.math.floor

class JDBCChatroomTransactionSpec: ChatroomTransactionsSpec() {

    companion object {
        val amqpConnectionFactory: ConnectionFactory by lazy {
            AMQPManager.connectionFactory()
        }

        val port = floor((Math.random() * 8000)).toInt() + 2000

        val application  by lazy { Async.awaitSucceededFuture(startApplication(port))!! }
    }

    override fun beforeSpec(description: Description, spec: Spec) {
        super.beforeSpec(description, spec)
        AMQPManager.cleanConnection(amqpConnectionFactory).close()
    }

    override fun afterSpec(description: Description, spec: Spec) {
        super.afterSpec(description, spec)
        AMQPManager.cleanConnection(amqpConnectionFactory).close()
    }

    override fun application(): ExampleApplication = application
}