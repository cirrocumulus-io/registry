package io.cirrocumulus.registry.api

import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
@Suppress("ClassName", "MemberVisibilityCanBePrivate")
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
            val user = repository.findByCredentials("username", "password")
            user shouldBe null
        }

        @Test
        fun `should return null if password does not match`() = runBlocking {
            val user = repository.findByCredentials("admin", "password")
            user shouldBe null
        }

        @Test
        fun `should return user if credentials are valid`() = runBlocking {
            val user = repository.findByCredentials("admin", "changeit")
            user shouldNotBe null
            user!! should {
                it.username shouldBe "admin"
                it.password shouldBe "\$2a\$12\$21SFFJSkRWjAeFt21v5mOe6lzDb7bvDfgcBVG66UB6/2mBYv8xOxS"
            }
        }
    }
}
