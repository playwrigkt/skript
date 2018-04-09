package playwrigkt.skript.http

import org.funktionale.option.Option
import org.funktionale.option.toOption

sealed class HttpPathPart {
    abstract fun matches(rawPart: String): Boolean
    abstract fun parse(rawPart: String): Pair<String, String>

    data class Variable(val name: String): HttpPathPart() {
        override fun parse(rawPart: String): Pair<String, String> {
            return name to rawPart
        }

        override fun matches(rawPart: String): Boolean {
            return true
        }
    }

    data class Literal(val value: String): HttpPathPart() {
        override fun parse(rawPart: String): Pair<String, String> {
            return rawPart to rawPart
        }

        override fun matches(rawPart: String): Boolean {
            return value.equals(rawPart)
        }
    }
}

data class HttpPathRule(val rule: String) {
    val ruleParts: List<HttpPathPart> by lazy {
        rule.split("/")
                .map {
                    if (it.startsWith("{") && it.endsWith("}")) {
                        HttpPathPart.Variable(it.removePrefix("{").removeSuffix("}"))
                    } else {
                        HttpPathPart.Literal(it)
                    }
                }
    }

    private fun Pair<HttpPathPart, String>.matchPathPart(): Boolean = first.matches(second)
    private fun Pair<HttpPathPart, String>.partPathPart(): Pair<String, String> = first.parse(second)

    fun pathMatches(path: String): Boolean =
        path.toOption()
                .map { it.split("/") }
                .filter { pathParts -> pathParts.size == ruleParts.size }
                .map { ruleParts.zip(it) }
                .filter { it.all { it.matchPathPart() } }
                .isDefined()

    fun apply(path: String): Option<Map<String, String>>  =
        path.split("/").toOption()
                .filter { pathParts -> pathParts.size == ruleParts.size }
                .map { ruleParts.zip(it) }
                .filter { it.all { it.matchPathPart() } }
                .map { it.map { it.partPathPart() }.toMap() }
}