package io.cirrocumulus.registry.core

import java.net.URI
import java.time.OffsetDateTime
import java.util.*

data class ImageFormat(
    val id: UUID = UUID.randomUUID(),
    val version: ImageVersion,
    val type: Type,
    val uri: URI,
    val sha512: String,
    val creationDate: OffsetDateTime = OffsetDateTime.now()
) {
    enum class Type {
        Qcow2
    }
}
