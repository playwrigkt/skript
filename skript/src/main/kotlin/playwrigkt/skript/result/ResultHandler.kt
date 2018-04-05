package playwrigkt.skript.result

typealias ResultHandler<T> = (Result<T>) -> Unit
typealias AsyncHandler<I, O> = (I) -> AsyncResult<O>

