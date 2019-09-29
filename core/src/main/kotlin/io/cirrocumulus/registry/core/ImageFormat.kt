package io.cirrocumulus.registry.core

import java.net.URI
import java.time.ZonedDateTime
import java.util.*

data class ImageFormat(
    val id: UUID = UUID.randomUUID(),
    val type: Type,
    val url: URI,
    val sha512: String,
    val creationDate: ZonedDateTime
) {
    enum class Type {
        Qcow2;

        override fun toString() = name.toLowerCase()
    }
}
