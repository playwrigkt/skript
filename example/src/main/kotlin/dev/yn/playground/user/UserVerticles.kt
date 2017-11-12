package dev.yn.playground.user

import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.common.ApplicationContextProvider
import dev.yn.playground.consumer.alpha.ConsumerFactory
import dev.yn.playground.vertx.alpha.consumer.VertxConsumerFactory

val userCreatedAddress = "user.updated"
val userLoginAddress = "user.login"

fun userLoginConsumer(provider: ApplicationContextProvider): ConsumerFactory<ApplicationContext> {
    return VertxConsumerFactory(userLoginAddress, provider)
}

fun userCreateConsumer(provider: ApplicationContextProvider): ConsumerFactory<ApplicationContext> {
    return VertxConsumerFactory(userCreatedAddress, provider)
}