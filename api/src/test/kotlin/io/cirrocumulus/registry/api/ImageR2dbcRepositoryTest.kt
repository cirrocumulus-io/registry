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
import java.net.URI
import java.time.ZoneOffset

@Suppress("ClassName")
class ImageR2dbcRepositoryTest {
    lateinit var repository: ImageR2dbcRepository

    @BeforeEach
    fun beforeEach() {
        repository = ImageR2dbcRepository(DbClient)
    }

    @Nested
    inner class create {
        val image = Image(
            ownerId = User1.id,
            group = User1.username,
            name = "fedora"
        )

        @Test
        fun `should insert image`() = runBlocking {
            repository.create(image) shouldBe image
        }
    }

    @Nested
    inner class createFormat {
        val format = ImageFormat(
            version = ImageVersion1_1,
            type = ImageFormat.Type.Qcow2,
            uri = URI("/v1/user1/fedora/30/qcow2"),
            sha512 = "8f14ceb5224148cd03648aed62803ef9b1155062d1f685b3945f22e9298e8bdfa68d3372864b6b0dcc205e3e2da7befb439dfdd3c245ce9f20726936a612664d"
        )

        @Test
        fun `should insert image format`() = runBlocking {
            repository.createFormat(format) shouldBe format
        }
    }

    @Nested
    inner class createVersion {
        val version = ImageVersion(
            image = Image1_1,
            name = "30"
        )

        @Test
        fun `should insert image version`() = runBlocking {
            repository.createVersion(version) shouldBe version
        }
    }

    @Nested
    inner class find {
        @Test
        fun `should return null if group does not exist`() = runBlocking {
            val image = repository.find(
                "${Image1_1.group}1",
                Image1_1.name
            )
            image.shouldBeNull()
        }

        @Test
        fun `should return null if name does not exist`() = runBlocking {
            val image = repository.find(
                Image1_1.group,
                "${Image1_1.name}1"
            )
            image.shouldBeNull()
        }

        @Test
        fun `should return image if it exists`() = runBlocking {
            val image = repository.find(
                Image1_1.group,
                Image1_1.name
            )
            image.atUtc() shouldBe Image1_1.atUtc()
        }
    }

    @Nested
    inner class findFormat {
        @Test
        fun `should return null if group does not exist`() = runBlocking {
            val imageFormat = repository.findFormat(
                "${Image1_1.group}1",
                Image1_1.name,
                ImageVersion1_1.name,
                ImageFormat1_1.type
            )
            imageFormat.shouldBeNull()
        }

        @Test
        fun `should return null if name does not exist`() = runBlocking {
            val imageFormat = repository.findFormat(
                Image1_1.group,
                "${Image1_1.name}1",
                ImageVersion1_1.name,
                ImageFormat1_1.type
            )
            imageFormat.shouldBeNull()
        }

        @Test
        fun `should return null if version does not exist`() = runBlocking {
            val imageFormat = repository.findFormat(
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
            val imageFormat = repository.findFormat(
                Image1_1.group,
                Image1_1.name,
                ImageVersion1_1.name,
                ImageFormat1_1.type
            )
            imageFormat.atUtc() shouldBe ImageFormat1_1.atUtc()
        }
    }

    @Nested
    inner class findVersion {
        @Test
        fun `should return null if group does not exist`() = runBlocking {
            val imageVersion = repository.findVersion(
                "${Image1_1.group}1",
                Image1_1.name,
                ImageVersion1_1.name
            )
            imageVersion.shouldBeNull()
        }

        @Test
        fun `should return null if name does not exist`() = runBlocking {
            val imageVersion = repository.findVersion(
                Image1_1.group,
                "${Image1_1.name}1",
                ImageVersion1_1.name
            )
            imageVersion.shouldBeNull()
        }

        @Test
        fun `should return null if version does not exist`() = runBlocking {
            val imageVersion = repository.findVersion(
                Image1_1.group,
                Image1_1.name,
                "${ImageVersion1_1.name}1"
            )
            imageVersion.shouldBeNull()
        }

        @Test
        fun `should return image version if it exists`() = runBlocking {
            val imageVersion = repository.findVersion(
                Image1_1.group,
                Image1_1.name,
                ImageVersion1_1.name
            )
            imageVersion.atUtc() shouldBe ImageVersion1_1.atUtc()
        }
    }

    fun Image?.atUtc() = this?.copy(creationDate = creationDate.withOffsetSameInstant(ZoneOffset.UTC))

    fun ImageFormat?.atUtc() = this?.copy(
        version = version.atUtc()!!,
        creationDate = creationDate.withOffsetSameInstant(ZoneOffset.UTC)
    )

    fun ImageVersion?.atUtc() = this?.copy(image = image.atUtc()!!)
}
