package io.cirrocumulus.registry.api

import io.ktor.auth.Principal
import java.util.*

data class UserPrincipal(
    val id: UUID,
    val username: String
) : Principal
