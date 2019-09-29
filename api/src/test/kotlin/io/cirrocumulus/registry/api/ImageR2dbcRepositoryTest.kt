package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.Image
import io.cirrocumulus.registry.core.ImageFormat
import io.cirrocumulus.registry.core.ImageVersion
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.ZoneOffset

@Suppress("ClassName")
class ImageR2dbcRepositoryTest {
    lateinit var repository: ImageR2dbcRepository

    @BeforeEach
    fun beforeEach() {
        repository = ImageR2dbcRepository(DbClient)
    }

    @Nested
    inner class find {
        @Test
        fun `should return null if group does not exist`() = runBlocking {
            val imageFormat = repository.find(
                "${Image1_1.group}1",
                Image1_1.name,
                ImageVersion1_1.name,
                ImageFormat1_1.type
            )
            imageFormat.shouldBeNull()
        }

        @Test
        fun `should return null if name does not exist`() = runBlocking {
            val imageFormat = repository.find(
                Image1_1.group,
                "${Image1_1.name}1",
                ImageVersion1_1.name,
                ImageFormat1_1.type
            )
            imageFormat.shouldBeNull()
        }

        @Test
        fun `should return null if version does not exist`() = runBlocking {
            val imageFormat = repository.find(
                Image1_1.group,
                Image1_1.name,
                "${ImageVersion1_1.name}1",
                ImageFormat1_1.type
            )
            imageFormat.shouldBeNull()
        }

        // TODO: Add test when format does not exist when many format will be supported

        @Test
        fun `should return image format if it exists`() = runBlocking {
            val imageFormat = repository.find(
                Image1_1.group,
                Image1_1.name,
                ImageVersion1_1.name,
                ImageFormat1_1.type
            )
            imageFormat.atUtc() shouldBe ImageFormat1_1.atUtc()
        }

        fun Image?.atUtc() = this?.copy(creationDate = creationDate.withOffsetSameInstant(ZoneOffset.UTC))

        fun ImageFormat?.atUtc() = this?.copy(
            version = version.atUtc()!!,
            creationDate = creationDate.withOffsetSameInstant(ZoneOffset.UTC)
        )

        fun ImageVersion?.atUtc() = this?.copy(image = image.atUtc()!!)
    }
}
