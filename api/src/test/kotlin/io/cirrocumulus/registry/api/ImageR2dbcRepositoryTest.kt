package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.Image
import io.cirrocumulus.registry.core.ImageFormat
import io.cirrocumulus.registry.core.ImageVersion
import io.cirrocumulus.registry.core.User
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.ZoneOffset
import java.util.*

@Suppress("ClassName")
class ImageR2dbcRepositoryTest : R2dbcRepositoryTest() {
    val user = User(
        id = UUID.fromString("ad7e59ae-05de-4c01-aec9-ecbbaebf2e36"),
        username = "user1",
        password = "\$2a\$12\$21SFFJSkRWjAeFt21v5mOe6lzDb7bvDfgcBVG66UB6/2mBYv8xOxS"
    )
    val format = ImageFormat(
        id = UUID.fromString("e9027812-ac20-49e7-9775-04befed94bbc"),
        version = ImageVersion(
            id = UUID.fromString("5bca81cd-a43a-468d-9c5d-be14b29d29d2"),
            image = Image(
                id = UUID.fromString("c4a6e944-627a-4556-958d-fffb1c59ac5d"),
                ownerId = user.id,
                group = user.username,
                name = "fedora"
            ),
            name = "30"
        ),
        type = ImageFormat.Type.Qcow2,
        uri = URI("/v1/user1/fedora/30/qcow2"),
        sha512 = "8f14ceb5224148cd03648aed62803ef9b1155062d1f685b3945f22e9298e8bdfa68d3372864b6b0dcc205e3e2da7befb439dfdd3c245ce9f20726936a612664d"
    )

    lateinit var repository: ImageR2dbcRepository

    @BeforeEach
    fun beforeEach() = runBlocking {
        repository = ImageR2dbcRepository(DbClient)
        insertUser(user)
    }

    @AfterEach
    fun afterEach() = runBlocking { deleteUser(user) }

    @Nested
    inner class create {
        @Test
        fun `should insert image`() = runBlocking {
            repository.create(format.image) shouldBe format.image
        }
    }

    @Nested
    inner class createFormat {
        @BeforeEach
        fun beforeEach() = runBlocking {
            insertImage(format.image)
            insertVersion(format.version)
        }

        @Test
        fun `should insert image format`() = runBlocking {
            repository.createFormat(format) shouldBe format
        }
    }

    @Nested
    inner class createVersion {
        @BeforeEach
        fun beforeEach() = runBlocking { insertImage(format.image) }

        @Test
        fun `should insert image version`() = runBlocking {
            repository.createVersion(format.version) shouldBe format.version
        }
    }

    @Nested
    inner class find {
        @BeforeEach
        fun beforeEach() = runBlocking { insertImage(format.image) }

        @Test
        fun `should return null if group does not exist`() = runBlocking {
            repository.find("group", format.imageName).shouldBeNull()
        }

        @Test
        fun `should return null if name does not exist`() = runBlocking {
            repository.find(format.imageGroup, "name").shouldBeNull()
        }

        @Test
        fun `should return image if it exists`() = runBlocking {
            repository.find(format.imageGroup, format.imageName).atUtc() shouldBe format.image.atUtc()
        }
    }

    @Nested
    inner class findFormat {
        @BeforeEach
        fun beforeEach() = runBlocking {
            insertImage(format.image)
            insertVersion(format.version)
            insertFormat(format)
        }

        @Test
        fun `should return null if group does not exist`() = runBlocking {
            repository
                .findFormat("group", format.imageName, format.versionName, format.type)
                .shouldBeNull()
        }

        @Test
        fun `should return null if name does not exist`() = runBlocking {
            repository
                .findFormat(format.imageGroup, "name", format.versionName, format.type)
                .shouldBeNull()
        }

        @Test
        fun `should return null if version does not exist`() = runBlocking {
            repository
                .findFormat(format.imageGroup, format.imageName, "version", format.type)
                .shouldBeNull()
        }

        // TODO: Add test when format does not exist when many format will be supported

        @Test
        fun `should return image format if it exists`() = runBlocking {
            repository
                .findFormat(format.imageGroup, format.imageName, format.versionName, format.type)
                .atUtc()
                .shouldBe(format.atUtc())
        }
    }

    @Nested
    inner class findVersion {
        @BeforeEach
        fun beforeEach() = runBlocking {
            insertImage(format.image)
            insertVersion(format.version)
        }

        @Test
        fun `should return null if group does not exist`() = runBlocking {
            repository.findVersion("group", format.imageName, format.versionName).shouldBeNull()
        }

        @Test
        fun `should return null if name does not exist`() = runBlocking {
            repository.findVersion(format.imageGroup, "name", format.versionName).shouldBeNull()
        }

        @Test
        fun `should return null if version does not exist`() = runBlocking {
            repository.findVersion(format.imageGroup, format.imageName, "version").shouldBeNull()
        }

        @Test
        fun `should return image version if it exists`() = runBlocking {
            repository.findVersion(format.imageGroup, format.imageName, format.versionName)
                .atUtc()
                .shouldBe(format.version.atUtc())
        }
    }

    fun Image?.atUtc() = this?.copy(creationDate = creationDate.withZoneSameInstant(ZoneOffset.UTC))

    fun ImageFormat?.atUtc() = this?.copy(
        version = version.atUtc()!!,
        creationDate = creationDate.withZoneSameInstant(ZoneOffset.UTC)
    )

    fun ImageVersion?.atUtc() = this?.copy(image = image.atUtc()!!)
}
