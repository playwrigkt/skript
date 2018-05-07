package playwrigkt.skript.ex

import org.funktionale.tries.Try

data class AggregateException(val errors: List<Throwable>): Exception()

fun <T: Any> List<Try<T>>.lift(): Try<List<T>> {
    val errors = this
            .mapNotNull { it.failed().toOption().orNull() }
            .flatMap {
                when {
                    it is AggregateException -> it.errors
                    else -> listOf(it)
                }
            }

    if(errors.isEmpty()) {
        return Try.Success(this.mapNotNull { it.toOption().orNull() })
    } else {
        return Try.Failure(AggregateException(errors))
    }
}