package io.cirrocumulus.registry.core

import java.time.OffsetDateTime
import java.util.*

data class Image(
    val id: UUID,
    val ownerId: UUID,
    val group: String,
    val name: String,
    val creationDate: OffsetDateTime
)
