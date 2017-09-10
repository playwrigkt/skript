package dev.yn.playground.sql

sealed class SQLError: Throwable() {
    data class UpdateFailed(val action: SQLAction.Update<*, *>): SQLError()
}