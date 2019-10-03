package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.Image
import io.cirrocumulus.registry.core.ImageFormat
import io.cirrocumulus.registry.core.ImageVersion

interface ImageRepository {
    suspend fun create(image: Image): Image

    suspend fun createFormat(format: ImageFormat): ImageFormat

    suspend fun createVersion(version: ImageVersion): ImageVersion

    suspend fun find(group: String, name: String): Image?

    suspend fun findFormat(group: String, name: String, versionName: String, formatType: ImageFormat.Type): ImageFormat?

    suspend fun findVersion(group: String, name: String, versionName: String): ImageVersion?
}
