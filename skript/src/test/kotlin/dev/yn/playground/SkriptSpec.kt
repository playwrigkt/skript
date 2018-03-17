package dev.yn.playground

import dev.yn.playground.ex.andThen
import dev.yn.playground.result.AsyncResult
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import org.funktionale.either.Either

class SkriptSpec : StringSpec() {
    init {
        "a skript can have no context" {
            val skript = Skript.map<Int, String, Unit> { it.toString() }
            skript.run(10, Unit) shouldBe AsyncResult.succeeded("10")
        }

        "a skript can be chained" {
            val skript = Skript
                    .map<Int, String, Unit> { it.toString() }
                    .map { it.toLong() * 2 }
            skript.run(10, Unit) shouldBe AsyncResult.succeeded(20L)
        }

        "a skript context can provide properties" {
            data class Config(val appName: String)
            data class ConfigContext(val config: Config)

            val skript =
                    Skript.map<Int, String, ConfigContext>{ it.toString() }
                            .mapWithContext { i, c -> "Application ${c.config.appName} received $i" }
            skript.run(10, ConfigContext(Config("Example"))) shouldBe AsyncResult.succeeded("Application Example received 10")
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

            val skript = Skript.branch(rightIfGreaterThanTen)
                    .left(double)
                    .right(half)

            skript.run(5, Unit) shouldBe AsyncResult.succeeded(10)
            skript.run(16, Unit) shouldBe AsyncResult.succeeded(8)
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

            val skript = Skript.branch(rightIfGreaterThanTen)
                    .left(double.andThen(toLong()))
                    .right(stringLength.andThen(toLong()))

            skript.run(5, Unit) shouldBe AsyncResult.succeeded(10L)
            skript.run(16, Unit) shouldBe AsyncResult.succeeded(2L)
        }
    }
}