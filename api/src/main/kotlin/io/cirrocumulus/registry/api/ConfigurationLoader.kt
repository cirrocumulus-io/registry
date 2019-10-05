package io.cirrocumulus.registry.api

interface ConfigurationLoader {
    fun load(): Configuration
}
