package io.cirrocumulus.registry.api

class ConfigurationException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception()
