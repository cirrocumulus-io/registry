package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.Image
import io.cirrocumulus.registry.core.ImageFormat
import io.cirrocumulus.registry.core.ImageVersion
import io.kotlintest.matchers.file.shouldHaveParent
import io.kotlintest.should
import io.kotlintest.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URI
import java.util.*

@Suppress("ClassName")
class FilesystemImageFileManagerTest {
    val config = Configuration.Registry()
    val qcow2File = File(DefaultImageHandler::class.java.getResource("/test.qcow2").file)
    val format = ImageFormat(
        version = ImageVersion(
            image = Image(
                ownerId = UUID.randomUUID(),
                group = "user1",
                name = "fedora"
            ),
            name = "30"
        ),
        type = ImageFormat.Type.Qcow2,
        uri = URI("/v1/user1/fedora/30/qcow2"),
        sha512 = "8f14ceb5224148cd03648aed62803ef9b1155062d1f685b3945f22e9298e8bdfa68d3372864b6b0dcc205e3e2da7befb439dfdd3c245ce9f20726936a612664d"
    )

    lateinit var fileManager: FilesystemImageFileManager

    @BeforeEach
    fun beforeEach() {
        fileManager = FilesystemImageFileManager(config)
    }

    @Nested
    inner class write {
        @Test
        fun `should write input stream to file`() = runBlocking {
            val file = fileManager.write(
                format.imageGroup,
                format.imageName,
                format.versionName,
                format.type,
                qcow2File.inputStream()
            )
            file shouldHaveParent format.versionName
            file.parentFile should { parent ->
                parent shouldHaveParent format.imageName
                parent.parentFile shouldHaveParent config.storageDir.name
            }
            file.readBytes() shouldBe qcow2File.inputStream().readBytes()
        }
    }
}
