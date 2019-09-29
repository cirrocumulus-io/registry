package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.ImageFormat

interface ImageRepository {
    suspend fun exists(group: String, name: String, version: String, formatType: ImageFormat.Type): Boolean
}
