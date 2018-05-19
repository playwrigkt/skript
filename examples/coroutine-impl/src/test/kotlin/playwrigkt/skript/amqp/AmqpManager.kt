package playwrigkt.skript.amqp

import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import playwrigkt.skript.user.UserSkripts.userCreatedAddress
import playwrigkt.skript.user.UserSkripts.userLoginAddress

object AmqpManager {
    val amqpExchange = "events.ex"

    fun connectionFactory(): ConnectionFactory {
        val factory = ConnectionFactory()
        factory.username = "rabbitmq"
        factory.password = "rabbitmq"
        factory.host = "localhost"
        factory.port = 5672
        factory.virtualHost = "/"
        return factory
    }

    fun cleanConnection(factory: ConnectionFactory): Connection {
        val connection = factory.newConnection()
        destroyAMQPResources(connection)
        initAMQPResources(connection)
        return connection
    }

    fun initAMQPResources(connection: Connection) {
        val channel = connection.createChannel()
        channel.exchangeDeclare(amqpExchange, BuiltinExchangeType.TOPIC)
        channel.queueDeclare(userLoginAddress, true, false, false, emptyMap())
        channel.queueDeclare(userCreatedAddress, true, false, false, emptyMap())
        channel.queueBind(userLoginAddress, amqpExchange, userLoginAddress)
        channel.queueBind(userCreatedAddress, amqpExchange, userCreatedAddress)
        channel.close()
    }

    fun destroyAMQPResources(connection: Connection) {
        val channel = connection.createChannel()
        channel.queueDelete(userLoginAddress)
        channel.queueDelete(userCreatedAddress)
        channel.exchangeDelete(amqpExchange)
        channel.close()
    }
}