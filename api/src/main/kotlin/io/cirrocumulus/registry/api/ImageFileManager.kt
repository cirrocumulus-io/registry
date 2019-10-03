package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.ImageFormat
import java.io.File
import java.io.InputStream

interface ImageFileManager {
    suspend fun write(
        group: String,
        name: String,
        version: String,
        formatType: ImageFormat.Type,
        imageFileInput: InputStream
    ): File
}
