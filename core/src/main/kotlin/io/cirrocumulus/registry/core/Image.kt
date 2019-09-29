package io.cirrocumulus.registry.core

import java.time.ZonedDateTime
import java.util.*

data class Image(
    val id: UUID,
    val ownerId: UUID,
    val group: String,
    val name: String,
    val versions: List<ImageVersion>,
    val creationDate: ZonedDateTime
)
