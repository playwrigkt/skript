package playwrigkt.skript

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec
import org.funktionale.either.Either
import org.funktionale.tries.Try
import playwrigkt.skript.ex.andThen
import playwrigkt.skript.ex.join
import playwrigkt.skript.result.CompletableResult
import java.text.DateFormat
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class SkriptSpec : StringSpec() {
    init {
        "an identity skript returns the  input" {
            val skript = Skript.identity<Int, Unit>()
            skript.run(15, Unit).result() shouldBe 15
        }

        "a map skript executes a  function on input" {
            val skript = Skript.map<Int, String, Unit> { it.toString() }
            skript.run(10, Unit).result() shouldBe "10"
        }

        "a map skript can have access to the troupe" {
            val skript = Skript.mapWithTroupe { date: Date, troupe: DateFormat ->
                troupe.format(date)
            }

            val dateFormat = DateFormat.getDateInstance()
            val date = Date()
            skript.run(date, dateFormat).result() shouldBe dateFormat.format(date)
        }

        "a map skript can handle  a Try" {
            val skript = Skript.mapTry<String, Long, Unit> { it: String ->
                Try { it.toLong() }
            }

            skript.run("dddd", Unit).error() shouldNotBe null
            skript.run("1234", Unit).result() shouldBe 1234L
        }

        "a skript can be chained" {
            val skript = Skript
                    .map<Int, String, Unit> { it.toString() }
                    .map { it.toLong() * 2 }
            skript.run(10, Unit).result() shouldBe 20L
        }

        "a skript can chain  an asynchronous action" {
            val executor = Executors
                    .newSingleThreadScheduledExecutor()
            val latch = CountDownLatch(1)
            val skript = Skript
                    .map<Int, String, Unit> { it.toString() }
                    .flatMap {
                        val result = CompletableResult<Long>()
                        executor.schedule({
                            Try { it.toLong() *  2 }
                                    .onSuccess(result::succeed)
                                    .onFailure(result::fail)
                            latch.countDown()
                        }, 50, TimeUnit.MILLISECONDS)
                        result }

            val async = skript.run(1000, Unit)
            async.isComplete() shouldBe false
            latch.await()
            async.result() shouldBe 2000L
        }

        "a skript can chain  an asynchronous action with the troupe" {
            val latch = CountDownLatch(1)
            val skript = Skript
                    .map<Int, String, ScheduledExecutorService> { it.toString() }
                    .flatMapWithTroupe { string, executor ->
                        val result = CompletableResult<Long>()
                        executor.schedule({
                            Try { string.toLong() *  2 }
                                    .onSuccess(result::succeed)
                                    .onFailure(result::fail)
                            latch.countDown()
                        }, 50, TimeUnit.MILLISECONDS)
                        result }

            val executor = Executors
                    .newSingleThreadScheduledExecutor()

            val async = skript.run(1000, executor)
            async.isComplete() shouldBe false
            latch.await()
            async.result() shouldBe 2000L
        }

        "a skript stage can provide properties" {
            data class Config(val appName: String)
            data class ConfigTroupe(val config: Config)

            val skript =
                    Skript.map<Int, String, ConfigTroupe>{ it.toString() }
                            .mapWithTroupe { i, c -> "Application ${c.config.appName} received $i" }
            skript.run(10, ConfigTroupe(Config("Example"))).result() shouldBe "Application Example received 10"
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

            skript.run(5, Unit).result() shouldBe 10
            skript.run(16, Unit).result() shouldBe 8
        }

        "A skript can transform and branch based on the result of a skript" {
            val double: Skript<Double, Double, Unit> = Skript.map { it * 2 }
            val stringLength = Skript.map<String, Int, Unit> { it.length }
            val toLong: Skript<Number, Long, Unit> = Skript.map { it.toLong() }

            val rightIfGreaterThanTen = Skript.map<Int, Either<Double, String>, Unit> {
                if(it > 10) {
                    Either.right(it.toString())
                } else {
                    Either.left(it.toDouble())
                }
            }

            val skript = Skript.branch(rightIfGreaterThanTen)
                    .left(double.andThen(toLong))
                    .right(stringLength.andThen(toLong))

            skript.run(5, Unit).result() shouldBe 10L
            skript.run(16, Unit).result() shouldBe 2L
        }

        "A skript can branch with a single method" {
            val double: Skript<Double, Double, Unit> = Skript.map { it * 2 }
            val stringLength = Skript.map<String, Int, Unit> { it.length }
            val toLong: Skript<Number, Long, Unit> = Skript.map { it.toLong() }

            val rightIfGreaterThanTen = Skript.map<Int, Either<Double, String>, Unit> {
                if(it > 10) {
                    Either.right(it.toString())
                } else {
                    Either.left(it.toDouble())
                }
            }

            val skript = Skript.branch(
                    control = rightIfGreaterThanTen,
                    left = double.andThen(toLong),
                    right = stringLength.andThen(toLong))

            skript.run(5, Unit).result() shouldBe 10L
            skript.run(16, Unit).result() shouldBe 2L
        }

        "Two skripts can be run concurrently" {
            val sumSkript: Skript<List<Int>, Int, Unit> = Skript.map { it.sum() }
            val lengthSkript: Skript<List<*>, Int, Unit> = Skript.map { it.size }

            val average = Skript.both(sumSkript, lengthSkript).join { sum, length -> sum.toDouble() / length }

            val input = listOf(1, 3, 5, 6, 3)
            average.run(input, Unit).result() shouldBe input.average()
        }

        "A skript can split and join" {
            val initialSkript = Skript.identity<String, Unit>()
            val mapping: Skript<String, Int, Unit> = Skript.map { it.length }

            val skript = initialSkript
                    .split(mapping)
                    .join { str, length -> "$str is $length characters long" }

            skript.run("abcde", Unit).result() shouldBe "abcde is 5 characters long"
        }
    }
}