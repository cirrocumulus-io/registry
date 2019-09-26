package io.cirrocumulus.registry.core

import java.util.*

data class User(
    val id: UUID = UUID.randomUUID(),
    val username: String,
    val password: String
)
