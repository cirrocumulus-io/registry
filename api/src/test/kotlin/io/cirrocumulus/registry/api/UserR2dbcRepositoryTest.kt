package io.cirrocumulus.registry.api

import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class UserR2dbcRepositoryTest {
    val dbClient = Configuration.Database(
        username = "cirrocumulus_registry",
        password = "cirrocumulus_registry"
    ).createClient()

    lateinit var repository: UserR2dbcRepository

    @BeforeEach
    fun beforeEach() {
        repository = UserR2dbcRepository(dbClient)
    }

    @Nested
    inner class findByCredentials {
        @Test
        fun `should return null if username does not exist`() = runBlocking {
            val user = repository.findByCredentials("${User1.username}1", ClearPassword)
            user.shouldBeNull()
        }

        @Test
        fun `should return null if password does not match`() = runBlocking {
            val user = repository.findByCredentials(User1.username, "${ClearPassword}1")
            user.shouldBeNull()
        }

        @Test
        fun `should return user if credentials are valid`() = runBlocking {
            val user = repository.findByCredentials(User1.username, ClearPassword)
            user shouldBe User1
        }
    }
}
