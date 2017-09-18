package dev.yn.playground.user

import io.vertx.core.AbstractVerticle

val userCreatedAddress = "user.updated"
val userLoginAddress = "user.login"

class UserUpdatedProcessingVerticle: AbstractVerticle() {
    override fun start() {
        vertx.eventBus().consumer<String>(userCreatedAddress) {
            println("it was updated: ${it.body()}")
            it.reply("me too, thanks")
        }
    }
}

class UserLoginProcessingVerticle: AbstractVerticle() {
    override fun start() {
        vertx.eventBus().consumer<String>(userLoginAddress) {
            println("it logged in: ${it.body()}")
            it.reply("me too, thanks")
        }
    }
}