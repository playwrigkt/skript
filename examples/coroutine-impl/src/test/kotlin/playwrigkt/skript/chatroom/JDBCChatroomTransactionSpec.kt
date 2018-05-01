package playwrigkt.skript.chatroom

import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import io.kotlintest.Description
import io.kotlintest.Spec
import playwrigkt.skript.ExampleApplication
import playwrigkt.skript.coroutine.AMQPManager
import playwrigkt.skript.coroutine.startApplication
import kotlin.math.floor

class JDBCChatroomTransactionSpec: ChatroomTransactionsSpec() {

    companion object {
        val amqpConnectionFactory: ConnectionFactory by lazy {
            AMQPManager.connectionFactory()
        }

        val hikariDSConfig: HikariConfig by lazy {
            val config = HikariConfig()
            config.jdbcUrl = "jdbc:postgresql://localhost:5432/chitchat"
            config.username = "chatty_tammy"
            config.password = "gossipy"
            config.driverClassName = "org.postgresql.Driver"
            config.maximumPoolSize = 1
            config.poolName = "test_pool"
            config
        }

        val port = floor((Math.random() * 8000)).toInt() + 2000

        val application  by lazy { startApplication(amqpConnectionFactory, hikariDSConfig, port) }
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