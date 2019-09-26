package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.User

interface UserRepository {
    suspend fun findByCredentials(username: String, password: String): User?
}
