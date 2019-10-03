package io.cirrocumulus.registry.core

import java.util.*

data class ImageVersion(
    val id: UUID = UUID.randomUUID(),
    val image: Image,
    val name: String
) {
    val imageGroup get() = image.group
    val imageName get() = image.name
}
