package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.ImageFormat
import java.io.InputStream
import java.util.*

interface ImageHandler {
    suspend fun handleUpload(
        userId: UUID,
        group: String,
        name: String,
        version: String,
        imageOriginalFilename: String,
        imageInput: InputStream
    ): ImageFormat
}
