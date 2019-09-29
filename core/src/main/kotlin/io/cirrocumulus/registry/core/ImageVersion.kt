package io.cirrocumulus.registry.core

import java.util.*

data class ImageVersion(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val formats: Set<ImageFormat>
)
