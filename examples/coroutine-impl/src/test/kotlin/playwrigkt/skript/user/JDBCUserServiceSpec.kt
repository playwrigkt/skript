package playwrigkt.skript.user

import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import io.kotlintest.Description
import io.kotlintest.Spec
import playwrigkt.skript.Async
import playwrigkt.skript.ExampleApplication
import playwrigkt.skript.coroutine.AMQPManager
import playwrigkt.skript.coroutine.startApplication
import kotlin.math.floor

class JDBCUserServiceSpec: UserServiceSpec() {
    companion object {
        val amqpConnectionFactory: ConnectionFactory by lazy {
            AMQPManager.connectionFactory()
        }

        val port = floor((Math.random() * 8000)).toInt() + 2000

        val application by lazy { Async.awaitSucceededFuture(startApplication(port))!! }
        val userHttpClient = UserHttpClient(port)
    }

    override fun userHttpClient(): UserHttpClient = userHttpClient
    override fun application(): ExampleApplication = application

    override fun beforeSpec(description: Description, spec: Spec) {
        super.beforeSpec(description, spec)
        AMQPManager.cleanConnection(amqpConnectionFactory).close()
    }
    override fun afterSpec(description: Description, spec: Spec) {
        super.afterSpec(description, spec)
        AMQPManager.cleanConnection(amqpConnectionFactory).close()
    }
}