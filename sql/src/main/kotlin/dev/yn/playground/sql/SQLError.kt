package dev.yn.playground.sql

sealed class SQLError: Throwable() {
    data class UpdateFailed(val mapping: SQLMapping<*, *, *>, val i: Any): SQLError()
    data class OnStatement(val statement: SQLStatement, val underlying: Throwable): SQLError()
}