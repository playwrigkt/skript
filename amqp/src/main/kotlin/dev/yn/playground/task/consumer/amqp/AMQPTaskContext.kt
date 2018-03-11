package dev.yn.playground.task.consumer.amqp

import com.rabbitmq.client.impl.AMQBasicProperties
import com.rabbitmq.client.impl.AMQConnection

interface AMQPTaskContext {
    fun getAMQPClient(): AMQConnection

    fun doit()
    {

        getAMQPClient().createChannel()
    }

}