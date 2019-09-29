package io.cirrocumulus.registry.api

import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class ImageR2dbcRepositoryTest {
    lateinit var repository: ImageR2dbcRepository

    @BeforeEach
    fun beforeEach() {
        repository = ImageR2dbcRepository(DbClient)
    }

    @Nested
    inner class exists {
        @Test
        fun `should return false if group does not exist`() = runBlocking {
            val exists = repository.exists(
                "${Image1_1.group}1",
                Image1_1.name,
                ImageVersion1_1.name,
                ImageFormat1_1.type
            )
            exists.shouldBeFalse()
        }

        @Test
        fun `should return false if name does not exist`() = runBlocking {
            val exists = repository.exists(
                Image1_1.group,
                "${Image1_1.name}1",
                ImageVersion1_1.name,
                ImageFormat1_1.type
            )
            exists.shouldBeFalse()
        }

        @Test
        fun `should return false if version does not exist`() = runBlocking {
            val exists = repository.exists(
                Image1_1.group,
                Image1_1.name,
                "${ImageVersion1_1.name}1",
                ImageFormat1_1.type
            )
            exists.shouldBeFalse()
        }

        // TODO: Add test when format does not exist when many format will be supported

        @Test
        fun `should return true if image exists`() = runBlocking {
            val exists = repository.exists(
                Image1_1.group,
                Image1_1.name,
                ImageVersion1_1.name,
                ImageFormat1_1.type
            )
            exists.shouldBeTrue()
        }
    }
}
