package playwrigkt.skript.serialize.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import playwrigkt.skript.config.ConfigValue
import playwrigkt.skript.ex.configValue
import java.math.BigDecimal
import java.math.BigInteger

class ConfigValueJacksonJsonDeserializerSpec: StringSpec() {
    init {
        val deserializer = ConfigValueJacksonJsonDeserializer()
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(KotlinModule())
        deserializer.register(objectMapper)

        val  writeAndParse: (ConfigValue) -> Unit = { configValue ->
            objectMapper.readValue(configValue.raw, ConfigValue::class.java) shouldBe configValue
        }

        "should parse a null value" {
            objectMapper.readValue("null", ConfigValue::class.java) shouldBe null
        }

        "should  write and parse null value" {
            objectMapper.readValue(ConfigValue.Empty.Null.raw, ConfigValue::class.java) shouldBe null
        }

        "should parse an integral number" {
            objectMapper.readValue("1", ConfigValue::class.java) shouldBe ConfigValue.Number(BigInteger.ONE)
        }

        "should write and parse an integral value" {
           writeAndParse(ConfigValue.Number(BigInteger.valueOf(1)))
        }

        "should parse a decimal number" {
            objectMapper.readValue("1.5", ConfigValue::class.java) shouldBe ConfigValue.Decimal(BigDecimal.valueOf(1.5))
        }

        "should write andd parse a decimal number" {
            writeAndParse(ConfigValue.Decimal(BigDecimal.valueOf(1.5)))
        }


        "should parse a string" {
            objectMapper.readValue("\"bees\"", ConfigValue::class.java) shouldBe ConfigValue.Text("bees")
        }

        "should write and parse a string" {
            writeAndParse(ConfigValue.Text("bees"))
        }

        "should parse a boolean" {
            objectMapper.readValue("true", ConfigValue::class.java) shouldBe ConfigValue.Bool(true)
        }

        "should write and parse a boolean" {
            writeAndParse(ConfigValue.Bool(true))
        }

        "should parse a list of nulls" {
            objectMapper.readValue("[null, null]", ConfigValue::class.java) shouldBe
                    ConfigValue.Collection.Array(listOf(ConfigValue.Empty.Null, ConfigValue.Empty.Null))
        }

        "should write and parse a list of nulls" {
            writeAndParse(ConfigValue.Collection.Array(listOf(ConfigValue.Empty.Null, ConfigValue.Empty.Null)))
        }
        "should parse a list of numbers" {
            objectMapper.readValue("[1, 2, 3, 4]", ConfigValue::class.java) shouldBe ConfigValue.Collection.Array(listOf(1.configValue(), 2.configValue(), 3.configValue(), 4.configValue()))
        }

        "should write and parse a list of numbers" {
            writeAndParse(ConfigValue.Collection.Array(listOf(1.configValue(), 2.configValue(), 3.configValue(), 4.configValue())))
        }

        "should parse a list of strings" {
            objectMapper.readValue("[\"bees\", \"knees\", \"trees\"]", ConfigValue::class.java) shouldBe ConfigValue.Collection.Array(listOf("bees".configValue(), "knees".configValue(), "trees".configValue()))
        }

        "should write and parse a list of strings" {
            writeAndParse(ConfigValue.Collection.Array(listOf("bees".configValue(), "knees".configValue(), "trees".configValue())))
        }

        "should parse a list of booleans" {
            objectMapper.readValue("[true, true, false]", ConfigValue::class.java) shouldBe listOf(true.configValue(), true.configValue(), false.configValue()).configValue()
        }

        "should write and parse a list of booleans" {
            writeAndParse(listOf(true.configValue(), true.configValue(), false.configValue()).configValue())
        }
        "should parse an empty object" {
            objectMapper.readValue("{}", ConfigValue::class.java) shouldBe
                    emptyMap<String, ConfigValue>().configValue()
        }

        "should write and parse an empty object" {
            writeAndParse(emptyMap<String, ConfigValue>().configValue())
        }

        "should parse an object with  a null value" {
            objectMapper.readValue("{\"optional\":null}", ConfigValue::class.java) shouldBe
                    mapOf("optional" to ConfigValue.Empty.Null).configValue()
        }

        "should write and parse an object with  a null value" {
            writeAndParse(mapOf("optional" to ConfigValue.Empty.Null).configValue())
        }

        "should parse an object with a string value" {
            objectMapper.readValue("{\"name\":\"cut\"}", ConfigValue::class.java) shouldBe
                    mapOf("name" to "cut".configValue()).configValue()
        }

        "should write and parse an object with a string value" {
            writeAndParse(mapOf("name" to "cut".configValue()).configValue())
        }

        "should parse an object with a numeric value" {
            objectMapper.readValue("{\"id\":12345}", ConfigValue::class.java) shouldBe
                    mapOf("id" to 12345.configValue()).configValue()
        }

        "should write and parse an object with a numeric value" {
            writeAndParse(mapOf("id" to 12345.configValue()).configValue())
        }

        "should parse an object with a decimal value" {
            objectMapper.readValue("{\"score\":44.7}", ConfigValue::class.java) shouldBe
                    mapOf("score" to 44.7.configValue()).configValue()
        }

        "should write and parse an object with a decimal value" {
            writeAndParse(
                    mapOf("score" to 44.7.configValue()).configValue())
        }

        "should parse an object with a boolean value" {
            objectMapper.readValue("{\"active\":true}", ConfigValue::class.java) shouldBe
                    mapOf("active" to true.configValue()).configValue()
        }

        "should write and parse an object with a boolean value" {
            writeAndParse(mapOf("active" to true.configValue()).configValue())
        }

        "should parse an object with an array of strings" {
            objectMapper.readValue("{\"attributes\":[\"contributor\",\"admin\"]}", ConfigValue::class.java) shouldBe
                    mapOf("attributes" to listOf("contributor".configValue(), "admin".configValue()).configValue()).configValue()
        }

        "should write and parse an object with an array of strings" {
            writeAndParse(mapOf("attributes" to listOf("contributor".configValue(), "admin".configValue()).configValue()).configValue())
        }

        "should parse an array  of objects" {
            objectMapper.readValue("[{\"name\":\"alice\"},{\"name\":\"ben\"},{\"name\":\"Cody\"},{\"name\":\"Lucy\"}]", ConfigValue::class.java) shouldBe
                    listOf(mapOf("name" to "alice".configValue()).configValue(),
                            mapOf("name" to "ben".configValue()).configValue(),
                            mapOf("name" to "Cody".configValue()).configValue(),
                            mapOf("name" to "Lucy".configValue()).configValue()).configValue()
        }

        "should write and parse an array  of objects" {
            writeAndParse(
                    listOf(mapOf("name" to "alice".configValue()).configValue(),
                            mapOf("name" to "ben".configValue()).configValue(),
                            mapOf("name" to "Cody".configValue()).configValue(),
                            mapOf("name" to "Lucy".configValue()).configValue()).configValue())
        }

        "should parse an array with variable types" {
            objectMapper.readValue("[" +
                    "\"keybley\"," +
                    "1345," +
                    "true," +
                    "{\"trackingId\":\"fff\"}]", ConfigValue::class.java) shouldBe
                    listOf("keybley".configValue(),
                            1345.configValue(),
                            true.configValue(),
                            mapOf("trackingId" to "fff".configValue()).configValue()
                    ).configValue()
        }

        "should write and parse an array with variable types" {
            writeAndParse(
                    listOf("keybley".configValue(),
                            1345.configValue(),
                            true.configValue(),
                            mapOf("trackingId" to "fff".configValue()).configValue()
                    ).configValue())
        }


        "should parse an object with  several fields of  different types" {
            writeAndParse(
                    mapOf("name" to "agate".configValue(),
                            "active" to true.configValue(),
                            "score" to 44.7.configValue(),
                            "id" to 1345.configValue(),
                            "token" to mapOf("pub_key" to "pubbb".configValue(), "value" to 23456.configValue()).configValue(),
                            "attributes" to listOf("sneaky".configValue(), "cumbersome".configValue()).configValue()
                    ).configValue())
        }


    }

}