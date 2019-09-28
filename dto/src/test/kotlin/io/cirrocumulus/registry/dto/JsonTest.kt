package io.cirrocumulus.registry.dto

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

abstract class JsonTest(
    val jsonName: String,
    val expectedDeserializedDto: Any,
    val type: KClass<*>
) {
    val mapper = ObjectMapper().findAndRegisterModules()

    @Test
    fun deserialization() {
        val dto = mapper.readValue(javaClass.getResourceAsStream("/json/$jsonName.json"), type.java)
        dto shouldBe expectedDeserializedDto
    }

    @Test
    fun serialization() {
        val json = mapper.writeValueAsString(expectedDeserializedDto)
        val dto = mapper.readValue(json, type.java)
        dto shouldBe expectedDeserializedDto
    }
}
