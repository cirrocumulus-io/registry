package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.ImageFormat
import java.io.InputStream

interface ImageHandler {
    suspend fun handleUpload(
        group: String,
        name: String,
        version: String,
        imageOriginalFilename: String,
        imageInput: InputStream
    ): ImageFormat
}
