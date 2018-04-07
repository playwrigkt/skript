package playwrigkt.skript.http

import org.funktionale.option.Option

internal sealed class HttpPathPart {
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

internal data class HttpPathRule(val rule: String) {
    val ruleParts: List<HttpPathPart> by lazy {
        rule.split("/")
                .map {
                    if(it.startsWith("{") && it.endsWith("}")) {
                        HttpPathPart.Variable(it.removePrefix("{").removeSuffix("}"))
                    } else {
                        HttpPathPart.Literal(it)
                    }
                }
    }

    private fun Pair<HttpPathPart, String>.matches(): Boolean = first.matches(second)
    private fun Pair<HttpPathPart, String>.parse(): Pair<String, String> = first.parse(second)

    fun matches(path: String): Boolean =
            Option.Some(path.split("/"))
                    .filter { pathParts -> pathParts.size == ruleParts.size }
                    .map { ruleParts.zip(it) }
                    .filter { it.all { it.matches() } }
                    .isDefined()

    fun apply(path: String): Option<Map<String, String>> =
            Option.Some(path.split("/"))
                    .filter { pathParts -> pathParts.size == ruleParts.size }
                    .map { ruleParts.zip(it) }
                    .filter { it.all { it.matches() } }
                    .map { it.map { it.parse() }.toMap() }
}