package playwrigkt.skript.common.models

data class Reference<ID, out Domain>(val id: ID, val referenced: Domain?) {
    companion object {
        fun <ID, Domain> defined(id: ID, referenced: Domain): Reference<ID, Domain> = Reference(id, referenced)
        fun <ID> empty(id: ID): Reference<ID, Nothing> = Reference(id, null)
    }
}
