package playwrigkt.skript.config

import org.funktionale.tries.Try
import java.math.BigDecimal
import java.math.BigInteger

sealed class ConfigValue {
    abstract val raw: String
    abstract fun propertiesList(propertyName: String? = null): List<Pair<String, String>>

    sealed class Error: Exception() {
        data class NotObject(val configValue: ConfigValue): Error()
        data class NotArray(val configValue: ConfigValue): Error()
        data class NotUndefined(val configValue: ConfigValue): Error()
        data class NotNull(val configValue: ConfigValue): Error()
        data class NotText(val configValue: ConfigValue): Error()
        data class NotNumber(val configValue: ConfigValue): Error()
        data class NotDecimal(val configValue: ConfigValue): Error()
        data class NotBool(val configValue: ConfigValue): Error()
        data class ValueNotFound(val path: String, val member: String): Error()
    }

    sealed class Empty: ConfigValue() {
        object Undefined: Empty() {
            override val raw: String = ""

            override fun propertiesList(propertyName: String?): List<Pair<String, String>> = emptyList()
        }

        object Null: Empty() {
            override val raw: String = "null"

            override fun propertiesList(propertyName: String?): List<Pair<String, String>> = emptyList()
        }
    }
    sealed class Collection: ConfigValue() {
        data class Object(val values: Map<String, ConfigValue>): Collection() {
            override val raw: String by lazy {
                "{${values.map { "\"${it.key}\":${it.value.raw}" }.joinToString(",")}}"
            }

            override fun propertiesList(propertyName: String?): List<Pair<String, String>> =
                    values
                            .flatMap { entry ->
                                entry.value.propertiesList(propertyName?.let { "$it.${entry.key}" }?:entry.key)
                            }
        }

        data class Array(val list: List<ConfigValue>): Collection() {
            override val raw: String by lazy {
                "[${list.map { it.raw }.joinToString(",")}]"
            }

            override fun propertiesList(propertyName: String?): List<Pair<String, String>> =
                    list
                            .mapIndexed { index, configValue ->
                                configValue.propertiesList(propertyName?.let {"$it.$index"}?:index.toString())
                            }
                            .flatten()
        }
    }

    data class Text(val value: String): ConfigValue() {
        override val raw: String = "\"$value\""

        override fun propertiesList(propertyName: String?): List<Pair<String, String>> = listOf((propertyName?:"") to value)
    }
    data class Number(val value: BigInteger): ConfigValue() {
        override val raw: String = value.toString()

        override fun propertiesList(propertyName: String?): List<Pair<String, String>> = listOf((propertyName?:"") to value.toString())
    }
    data class Decimal(val value: BigDecimal): ConfigValue() {
        override val raw: String  = value.toString()

        override fun propertiesList(propertyName: String?): List<Pair<String, String>> = listOf((propertyName?:"") to value.toString())
    }
    data class Bool(val value: Boolean): ConfigValue() {
        override val raw: String = value.toString()

        override fun propertiesList(propertyName: String?): List<Pair<String, String>> = listOf((propertyName?:"") to value.toString())
    }

    fun objekt(): Try<ConfigValue.Collection.Object> = when {
        this is ConfigValue.Collection.Object -> Try.Success(this)
        else -> Try.Failure(Error.NotObject(this))
    }

    fun array(): Try<Collection.Array> = when {
        this is ConfigValue.Collection.Array -> Try.Success(this)
        else -> Try.Failure(Error.NotArray(this))
    }

    fun undefined(): Try<Empty.Undefined> = when {
        this is ConfigValue.Empty.Undefined -> Try.Success(this)
        else -> Try.Failure(Error.NotUndefined(this))
    }

    fun nUll(): Try<Empty.Null> = when{
        this is ConfigValue.Empty.Null -> Try.Success(this)
        else -> Try.Failure(Error.NotNull(this))
    }

    fun text(): Try<Text> = when {
        this is Text -> Try.Success(this)
        else -> Try.Failure(Error.NotText(this))
    }

    fun number(): Try<Number> = when {
        this is Number -> Try.Success(this)
        else -> Try.Failure(Error.NotNumber(this))
    }

    fun decimal(): Try<Decimal> = when {
        this is Decimal -> Try.Success(this)
        else -> Try.Failure(Error.NotDecimal(this))
    }

    fun bool(): Try<Bool> = when {
        this is Bool -> Try.Success(this)
        else -> Try.Failure(Error.NotBool(this))
    }

    fun applyPath(path: String, delimiter: String): Try<ConfigValue> =
            path.split(delimiter)
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .fold(Try.Success(this)) { result: Try<ConfigValue>, nextPathItem: String ->
                    result.flatMap {
                        it.objekt()
                    }.flatMap {
                        it.values.get(nextPathItem)
                                ?.let { Try.Success(it) }
                                ?: Try.Failure<ConfigValue>(Error.ValueNotFound(path, nextPathItem))
                    }
                }
}