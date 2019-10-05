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
            val format = ImageFormat1_1
            val file = fileManager.write(
                format.imageGroup,
                format.imageName,
                format.versionName,
                format.type,
                Qcow2ImageFile.inputStream()
            )
            file shouldHaveParent format.versionName
            file.parentFile should { parent ->
                parent shouldHaveParent format.imageName
                parent.parentFile shouldHaveParent config.registry.storageDir.name
            }
            file.readBytes() shouldBe Qcow2ImageFile.inputStream().readBytes()
        }
    }
}
