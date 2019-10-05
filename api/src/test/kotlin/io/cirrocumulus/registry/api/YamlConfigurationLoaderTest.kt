package io.cirrocumulus.registry.api

import com.fasterxml.jackson.core.JsonProcessingException
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

@Suppress("ClassName")
class YamlConfigurationLoaderTest {
    lateinit var loader: YamlConfigurationLoader

    @Nested
    inner class load {
        @Test
        fun `should throw exception if yaml is invalid`() {
            loader = YamlConfigurationLoader(File(javaClass.getResource("/config/invalid").file))
            val exception = assertThrows<ConfigurationException> { loader.load() }
            exception.cause.shouldBeInstanceOf<JsonProcessingException>()
        }

        @Test
        fun `should load default config`() {
            loader = YamlConfigurationLoader(File(javaClass.getResource("/config/empty").file))
            loader.load() shouldBe Configuration()
        }

        @Test
        fun `should load partial config`() {
            loader = YamlConfigurationLoader(File(javaClass.getResource("/config/partial").file))
            loader.load() shouldBe Configuration(
                db = Configuration.Database(host = "127.0.0.1"),
                registry = Configuration.Registry(storageDir = File("/var/registry"))
            )
        }

        @Test
        fun `should load config`() {
            loader = YamlConfigurationLoader(File(javaClass.getResource("/config/valid").file))
            loader.load() shouldBe Configuration(
                db = Configuration.Database(
                    host = "127.0.0.1",
                    port = 5433,
                    name = "registry",
                    username = "registry",
                    password = "registry"
                ),
                registry = Configuration.Registry(
                    baseUrl = "http://localhost:8081",
                    storageDir = File("/var/registry")
                )
            )
        }
    }
}
