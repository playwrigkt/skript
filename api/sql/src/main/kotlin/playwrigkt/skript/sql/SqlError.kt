package playwrigkt.skript.sql

sealed class SqlError: Throwable() {
    data class UpdateMappingFailed(val mapping: SqlMapping<*, *, *, *>, val i: Any): SqlError()
    data class SqlRowError(val rowValue: Any?, override val message: String): SqlError()
    object RowIteratorEmpty: SqlError()
    data class OnCommand(val statement: SqlCommand, val underlying: Throwable): SqlError()
}