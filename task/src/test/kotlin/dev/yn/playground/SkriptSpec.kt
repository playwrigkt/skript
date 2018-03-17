package dev.yn.playground

import dev.yn.playground.ex.andThen
import dev.yn.playground.result.AsyncResult
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import org.funktionale.either.Either

class SkriptSpec : StringSpec() {
    init {
        "a skript can have no context" {
            val task = Skript.map<Int, String, Unit> { it.toString() }
            task.run(10, Unit) shouldBe AsyncResult.succeeded("10")
        }

        "a skript can be chained" {
            val task = Skript
                    .map<Int, String, Unit> { it.toString() }
                    .map { it.toLong() * 2 }
            task.run(10, Unit) shouldBe AsyncResult.succeeded(20L)
        }

        "a skript context can provide properties" {
            data class Config(val appName: String)
            data class ConfigContext(val config: Config)

            val task =
                    Skript.map<Int, String, ConfigContext>{ it.toString() }
                            .mapWithContext { i, c -> "Application ${c.config.appName} received $i" }
            task.run(10, ConfigContext(Config("Example"))) shouldBe AsyncResult.succeeded("Application Example received 10")
        }

        "A skript can branch based on the result of a skript" {
            val double = Skript.map<Int, Int, Unit> { it * 2 }
            val half = Skript.map<Int, Int, Unit> { it / 2 }
            val rightIfGreaterThanTen = Skript.map<Int, Either<Int, Int>, Unit> {
                if(it > 10) {
                    Either.right(it)
                } else {
                    Either.left(it)
                }
            }

            val task = Skript.branch(rightIfGreaterThanTen)
                    .left(double)
                    .right(half)

            task.run(5, Unit) shouldBe AsyncResult.succeeded(10)
            task.run(16, Unit) shouldBe AsyncResult.succeeded(8)
        }

        "A skript can transform and branch based on the result of a skript" {
            val double: Skript<Double, Double, Unit> = Skript.map { it * 2 }
            val stringLength = Skript.map<String, Int, Unit> { it.length }
            fun toLong(): Skript<Number, Long, Unit> = Skript.map { it.toLong() }

            val rightIfGreaterThanTen = Skript.map<Int, Either<Double, String>, Unit> {
                if(it > 10) {
                    Either.right(it.toString())
                } else {
                    Either.left(it.toDouble())
                }
            }

            val task = Skript.branch(rightIfGreaterThanTen)
                    .left(double.andThen(toLong()))
                    .right(stringLength.andThen(toLong()))

            task.run(5, Unit) shouldBe AsyncResult.succeeded(10L)
            task.run(16, Unit) shouldBe AsyncResult.succeeded(2L)
        }
    }
}