package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.ImageFormat

class DefaultImageHandler(
    private val imageRepository: ImageRepository
) : ImageHandler {
    override suspend fun handleUpload(group: String, name: String, version: String): ImageFormat {
        TODO()
    }
}
