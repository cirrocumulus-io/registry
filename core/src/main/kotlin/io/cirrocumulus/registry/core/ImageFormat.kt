package io.cirrocumulus.registry.core

import java.net.URI
import java.time.ZonedDateTime
import java.util.*

data class ImageFormat(
    val id: UUID = UUID.randomUUID(),
    val version: ImageVersion,
    val type: Type,
    val uri: URI,
    val sha512: String,
    val creationDate: ZonedDateTime = ZonedDateTime.now()
) {
    enum class Type(
        val fileExtension: String
    ) {
        Qcow2("qcow2")
    }

    val image get() = version.image
    val imageGroup get() = version.imageGroup
    val imageName get() = version.imageName
    val ownerId get() = image.ownerId
    val versionName get() = version.name
}
