package io.cirrocumulus.registry.core

import java.time.ZonedDateTime
import java.util.*

data class Image(
    val id: UUID = UUID.randomUUID(),
    val ownerId: UUID,
    val group: String,
    val name: String,
    val creationDate: ZonedDateTime = ZonedDateTime.now()
)
