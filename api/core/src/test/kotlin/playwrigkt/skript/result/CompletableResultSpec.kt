package playwrigkt.skript.result

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import java.util.concurrent.atomic.AtomicInteger

class CompletableResultSpec: StringSpec() {

    init {
        "A new completable result has no result or error" {
            val result = CompletableResult<Int>()
            result.result() shouldBe null
            result.error() shouldBe null
        }
        "A succeeded result has a result but no error" {
            val result = CompletableResult<Int>()
            result.succeed(5)
            result.result() shouldBe 5
            result.error() shouldBe null
        }
        "A failed result has an error bu no result" {
            val result = CompletableResult<Int>()
            val error = RuntimeException("ayyyye")
            result.fail(error)
            result.result() shouldBe null
            result.error() shouldBe error
        }
        "An result should invoke all handlers once it is complete" {
            val atom = AtomicInteger(0)
            val incrementAtom = { i: Int ->
                atom.addAndGet(i)
                atom.get()
            }
            val result = CompletableResult<Int>()
            val map1Result = result.map(incrementAtom)
            val map2Result = result.map(incrementAtom)
            val map3Result = result.map(incrementAtom)

            result.isComplete() shouldBe false
            map1Result.isComplete() shouldBe false
            map2Result.isComplete() shouldBe false
            map2Result.isComplete() shouldBe false

            result.succeed(1)
            result.result() shouldBe 1
            map1Result.result() shouldBe 1
            map2Result.result() shouldBe 2
            map3Result.result() shouldBe 3
        }

        "An result should invoke a handler if it is already complete" {
            val atom = AtomicInteger(0)
            val incrementAtom = { i: Int ->
                atom.addAndGet(i)
                atom.get()
            }
            val result = CompletableResult<Int>()
            result.succeed(1)
            val map1Result = result.map(incrementAtom)
            val map2Result = result.map(incrementAtom)
            val map3Result = result.map(incrementAtom)

            result.result() shouldBe 1
            map1Result.result() shouldBe 1
            map2Result.result() shouldBe 2
            map3Result.result() shouldBe 3
        }

        "A result should  throw an exception if succeed is called after it  has failed"{
            val result = CompletableResult<Int>()
            result.fail(RuntimeException("hello"))
            val error = shouldThrow<IllegalStateException> { result.succeed(1) }
            error.message shouldBe "Result is already complete"
        }

        "A result should  throw an exception if succeed is called after it  has succeeded"{
            val result = CompletableResult<Int>()
            result.succeed(1)
            val error = shouldThrow<IllegalStateException> { result.succeed(1) }
            error.message shouldBe "Result is already complete"
        }

        "A result should  throw an exception if fail is called after it  has failed"{
            val result = CompletableResult<Int>()
            result.fail(RuntimeException("hello"))
            val error = shouldThrow<IllegalStateException> { result.fail(RuntimeException("hello")) }
            error.message shouldBe "Result is already complete"
        }

        "A result should  throw an exception if fail is called after it  has succeeded"{
            val result = CompletableResult<Int>()
            result.succeed(1)
            val error = shouldThrow<IllegalStateException> { result.fail(RuntimeException("hello")) }
            error.message shouldBe "Result is already complete"
        }
    }
}