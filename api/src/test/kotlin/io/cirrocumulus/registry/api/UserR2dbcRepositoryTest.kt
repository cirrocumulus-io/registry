package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.User
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

@Suppress("ClassName")
class UserR2dbcRepositoryTest : R2dbcRepositoryTest() {
    val user = User(
        id = UUID.fromString("474462a6-c107-41ac-aafb-d86f38ac8e60"),
        username = "user1",
        password = "\$2a\$12\$21SFFJSkRWjAeFt21v5mOe6lzDb7bvDfgcBVG66UB6/2mBYv8xOxS"
    )
    lateinit var repository: UserR2dbcRepository

    @BeforeEach
    fun beforeEach() {
        repository = UserR2dbcRepository(DbClient)
    }

    @Nested
    inner class findByCredentials {
        @BeforeEach
        fun beforeEach() = runBlocking { insertUser(user) }

        @AfterEach
        fun afterEach() = runBlocking { deleteUser(user) }

        @Test
        fun `should return null if username does not exist`() = runBlocking {
            repository.findByCredentials("user", "password").shouldBeNull()
        }

        @Test
        fun `should return null if password does not match`() = runBlocking {
            repository.findByCredentials(user.username, "foo123").shouldBeNull()
        }

        @Test
        fun `should return user if credentials are valid`() = runBlocking {
            repository.findByCredentials(user.username, "changeit") shouldBe user
        }
    }
}
