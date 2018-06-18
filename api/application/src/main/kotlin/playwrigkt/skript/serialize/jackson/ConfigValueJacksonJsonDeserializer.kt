package playwrigkt.skript.serialize.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import playwrigkt.skript.config.ConfigValue
import playwrigkt.skript.ex.configValue


class ConfigValueJacksonJsonDeserializer: JsonDeserializer<ConfigValue>() {
    fun register(objectMapper: ObjectMapper): ObjectMapper {

        return objectMapper.registerModule(
                SimpleModule().addDeserializer(ConfigValue::class.java, this))
    }

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ConfigValue {
        return p?.let { parser ->
            deserialize(parser.codec.readTree(parser))
        } ?: ConfigValue.Empty.Undefined
    }

    private fun deserialize(node: JsonNode): ConfigValue =
        when {
            node.isIntegralNumber -> ConfigValue.Number(node.bigIntegerValue())
            node.isFloatingPointNumber -> ConfigValue.Decimal(node.decimalValue())
            node.isTextual -> ConfigValue.Text(node.textValue())
            node.isBoolean -> ConfigValue.Bool(node.booleanValue())
            node.isArray -> node.elements().asSequence()
                    .map(this::deserialize)
                    .toList().configValue()
            node.isObject -> node.fields().asSequence()
                    .map { it.key  to deserialize(it.value) }
                    .toMap().configValue()
            node.isNull -> ConfigValue.Empty.Null
            else -> throw RuntimeException("unhandled")
        }
}