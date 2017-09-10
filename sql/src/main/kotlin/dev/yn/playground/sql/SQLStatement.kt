package dev.yn.playground.sql

import io.vertx.core.json.JsonArray

sealed class SQLStatement {
    data class Parameterized(val query: String, val params: JsonArray): SQLStatement()
    data class Simple(val query: String): SQLStatement()
}