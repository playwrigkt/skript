package dev.yn.playground.sql

sealed class SQLError: Throwable() {
    data class UpdateFailed(val action: SQLAction<*, *>): SQLError()
    data class OnStatement(val statement: SQLStatement, val underlying: Throwable): SQLError()
}