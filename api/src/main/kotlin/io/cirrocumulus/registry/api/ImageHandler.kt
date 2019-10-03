package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.ImageFormat
import java.io.InputStream
import java.util.*

interface ImageHandler {
    suspend fun handleUpload(
        userId: UUID,
        group: String,
        name: String,
        versionName: String,
        imageOriginalFilename: String,
        imageFileInput: InputStream
    ): ImageFormat
}
