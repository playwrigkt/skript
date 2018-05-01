package playwrigkt.skript.http.server

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

/**
 * Server side http path binding.
 */
data class HttpPathRule(val rule: String,
                        val variablePrefix: String = "{",
                        val variableSuffix: String = "}") {
    val ruleParts: List<HttpPathPart> by lazy {
        rule.split("/")
                .map {
                    if (it.startsWith(variablePrefix) && it.endsWith(variableSuffix)) {
                        HttpPathPart.Variable(it.removePrefix(variablePrefix).removeSuffix(variableSuffix))
                    } else {
                        HttpPathPart.Literal(it)
                    }
                }
    }

    private fun Pair<HttpPathPart, String>.matchPathPart(): Boolean = first.matches(second)
    private fun Pair<HttpPathPart, String>.partPathPart(): Pair<String, String> = first.parse(second)


    /**
     * @return true if the requestPath matches the rule
     */
    fun pathMatches(requestPath: String): Boolean =
        requestPath.toOption()
                .map { it.split("/") }
                .filter { pathParts -> pathParts.size == ruleParts.size }
                .map { ruleParts.zip(it) }
                .filter { it.all { it.matchPathPart() } }
                .isDefined()

    /**
     * parse a given requestPath according to the rule
     *
     * @return map of requestPath variable name to requestPath variable value, or value to value if constant
     */
    fun apply(requestPath: String): Option<Map<String, String>>  =
        requestPath.split("/").toOption()
                .filter { pathParts -> pathParts.size == ruleParts.size }
                .map { ruleParts.zip(it) }
                .filter { it.all { it.matchPathPart() } }
                .map { it.map { it.partPathPart() }.toMap() }
}