package dev.yn.playground.user

import dev.yn.playground.common.ApplicationContext
import dev.yn.playground.common.ApplicationContextProvider
import dev.yn.playground.consumer.alpha.ConsumerExecutor
import dev.yn.playground.vertx.alpha.consumer.VertxConsumerExecutor

val userCreatedAddress = "user.updated"
val userLoginAddress = "user.login"

fun userLoginConsumer(provider: ApplicationContextProvider): ConsumerExecutor<ApplicationContext> {
    return VertxConsumerExecutor(userLoginAddress, provider)
}

fun userCreateConsumer(provider: ApplicationContextProvider): ConsumerExecutor<ApplicationContext> {
    return VertxConsumerExecutor(userCreatedAddress, provider)
}