package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.ImageFormat

interface ImageHandler {
    suspend fun handleUpload(group: String, name: String, version: String): ImageFormat
}
