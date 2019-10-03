package io.cirrocumulus.registry.api

import io.kotlintest.matchers.file.shouldHaveParent
import io.kotlintest.should
import io.kotlintest.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class FilesystemImageFileManagerTest {
    val config = Configuration()

    lateinit var fileManager: FilesystemImageFileManager

    @BeforeEach
    fun beforeEach() {
        fileManager = FilesystemImageFileManager(config)
    }

    @Nested
    inner class write {
        @Test
        fun `should write input stream to file`() = runBlocking {
            val imageFormat = ImageFormat1_1
            val file = fileManager.write(
                imageFormat.version.image.group,
                imageFormat.version.image.name,
                imageFormat.version.name,
                imageFormat.type,
                javaClass.getResourceAsStream("/test.qcow2")
            )
            file shouldHaveParent imageFormat.version.name
            file.parentFile should { parent ->
                parent shouldHaveParent imageFormat.version.image.name
                parent.parentFile shouldHaveParent config.registry.imagesDir.name
            }
            file.readBytes() shouldBe javaClass.getResourceAsStream("/test.qcow2").readBytes()
        }
    }
}
