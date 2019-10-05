package io.cirrocumulus.registry.api

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import java.io.File

class YamlConfigurationLoader : ConfigurationLoader {
    companion object {
        private val ConfigDir = File("etc")
        private const val DbConfigFilename = "database.yml"
        private const val RegistryConfigFilename = "registry.yml"

        private val Logger = LoggerFactory.getLogger(YamlConfigurationLoader::class.java)
    }

    private val configDir: File
    private val mapper = ObjectMapper(YAMLFactory()).findAndRegisterModules()

    constructor() : this(ConfigDir)

    internal constructor(configDir: File) {
        this.configDir = configDir
    }

    override fun load(): Configuration {
        val db = loadConfiguration(DbConfigFilename, Configuration.Database())
        val registry = loadConfiguration(RegistryConfigFilename, Configuration.Registry())
        return Configuration(
            db = db,
            registry = registry
        )
    }

    private inline fun <reified C> loadConfiguration(filename: String, defaultConfig: C): C {
        val configName = filename.removeSuffix(".yml")
        val file = configDir.resolve(filename)
        return if (!file.exists()) {
            Logger.info("Default {} configuration loaded", configName)
            defaultConfig
        } else {
            try {
                mapper.readValue<C>(file).apply {
                    Logger.info("{} configuration loaded from {}", configName.firstLetterUpperCase(), filename)
                }
            } catch (exception: JsonProcessingException) {
                val message = "Unable to load configuration ($file)"
                Logger.error(message, exception)
                throw ConfigurationException(message, exception)
            }
        }
    }

    private fun String.firstLetterUpperCase() = this[0].toUpperCase() + substring(1)
}
