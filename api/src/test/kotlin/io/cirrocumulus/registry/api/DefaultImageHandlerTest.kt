package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.Image
import io.cirrocumulus.registry.core.ImageFormat
import io.cirrocumulus.registry.core.ImageVersion
import io.kotlintest.matchers.file.shouldNotExist
import io.kotlintest.matchers.throwable.shouldHaveMessage
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.sql.SQLException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@Suppress("ClassName", "BlockingMethodInNonBlockingContext")
class DefaultImageHandlerTest {
    lateinit var imageRepository: ImageRepository
    lateinit var imageFileManager: ImageFileManager
    lateinit var clock: Clock

    lateinit var handler: DefaultImageHandler

    @BeforeEach
    fun beforeEach() {
        imageRepository = mockk()
        imageFileManager = mockk()
        clock = mockk {
            every { instant() } returns Instant.now()
            every { zone } returns ZoneId.systemDefault()
        }
        handler = DefaultImageHandler(imageRepository, imageFileManager, clock)
    }

    @Nested
    inner class handleUpload {
        @Test
        fun `should fail if format is unsupported`() {
            val exception = assertThrows<UnsupportedFormatException> {
                runBlocking {
                    handler.handleUpload(
                        User1.id,
                        ImageFormat1_1.imageGroup,
                        ImageFormat1_1.imageName,
                        ImageFormat1_1.versionName,
                        "test.txt",
                        Qcow2ImageFile.inputStream()
                    )
                }
            }
            exception shouldHaveMessage "txt is not a supported format"
        }

        @Test
        fun `should fail if format already exists`() {
            val format = ImageFormat1_1

            every {
                runBlocking {
                    imageRepository.findFormat(
                        format.imageGroup,
                        format.imageName,
                        format.versionName,
                        ImageFormat.Type.Qcow2
                    )
                }
            } returns format

            val exception = assertThrows<ImageFormatAlreadyExistsException> {
                runBlocking {
                    handler.handleUpload(
                        format.ownerId,
                        format.imageGroup,
                        format.imageName,
                        format.versionName,
                        Qcow2ImageFile.name,
                        Qcow2ImageFile.inputStream()
                    )
                }
            }
            exception.format shouldBe format
        }

        @Test
        fun `should remove file if database error occurred`() = runBlocking {
            val expectedImageFormat = ImageFormat1_1
            val imageInput = Qcow2ImageFile.inputStream()
            val file = Qcow2ImageFile.copyTo(File("${Qcow2ImageFile.parent}/test-copy.qcow2"))
            file.createNewFile()

            every {
                runBlocking {
                    imageRepository.findFormat(
                        expectedImageFormat.imageGroup,
                        expectedImageFormat.imageName,
                        expectedImageFormat.versionName,
                        expectedImageFormat.type
                    )
                }
            } returns null

            every {
                runBlocking {
                    imageFileManager.write(
                        expectedImageFormat.imageGroup,
                        expectedImageFormat.imageName,
                        expectedImageFormat.versionName,
                        expectedImageFormat.type,
                        imageInput
                    )
                }
            } returns file

            every {
                runBlocking {
                    imageRepository.findVersion(
                        expectedImageFormat.imageGroup,
                        expectedImageFormat.imageName,
                        expectedImageFormat.versionName
                    )
                }
            } returns expectedImageFormat.version

            every {
                runBlocking {
                    imageRepository.find(
                        expectedImageFormat.imageGroup,
                        expectedImageFormat.imageName
                    )
                }
            } returns expectedImageFormat.image

            val expectedException = SQLException()
            every { runBlocking { imageRepository.createFormat(any()) } } answers { throw expectedException }

            val exception = assertThrows<SQLException> {
                runBlocking {
                    handler.handleUpload(
                        expectedImageFormat.image.ownerId,
                        expectedImageFormat.imageGroup,
                        expectedImageFormat.imageName,
                        expectedImageFormat.versionName,
                        Qcow2ImageFile.name,
                        imageInput
                    )
                }
            }
            exception shouldBe expectedException
            file.shouldNotExist()
        }

        @Test
        fun `should return created image format`() = runBlocking {
            val expectedImageFormat = ImageFormat1_1
            val imageInput = Qcow2ImageFile.inputStream()

            every {
                runBlocking {
                    imageRepository.findFormat(
                        expectedImageFormat.imageGroup,
                        expectedImageFormat.imageName,
                        expectedImageFormat.versionName,
                        expectedImageFormat.type
                    )
                }
            } returns null

            every {
                runBlocking {
                    imageFileManager.write(
                        expectedImageFormat.imageGroup,
                        expectedImageFormat.imageName,
                        expectedImageFormat.versionName,
                        expectedImageFormat.type,
                        imageInput
                    )
                }
            } returns Qcow2ImageFile

            every {
                runBlocking {
                    imageRepository.findVersion(
                        expectedImageFormat.imageGroup,
                        expectedImageFormat.imageName,
                        expectedImageFormat.versionName
                    )
                }
            } returns expectedImageFormat.version

            every {
                runBlocking {
                    imageRepository.find(
                        expectedImageFormat.imageGroup,
                        expectedImageFormat.imageName
                    )
                }
            } returns expectedImageFormat.image

            val formatSlot = slot<ImageFormat>()
            every { runBlocking { imageRepository.createFormat(capture(formatSlot)) } } answers { formatSlot.captured }

            val format = handler.handleUpload(
                expectedImageFormat.image.ownerId,
                expectedImageFormat.imageGroup,
                expectedImageFormat.imageName,
                expectedImageFormat.versionName,
                Qcow2ImageFile.name,
                imageInput
            )
            format shouldBe ImageFormat1_1.copy(
                id = formatSlot.captured.id,
                creationDate = clock.instant().atZone(clock.zone)
            )
        }

        @Test
        fun `should create version and return created image format`() = runBlocking {
            val expectedImageFormat = ImageFormat1_1
            val imageInput = Qcow2ImageFile.inputStream()

            every {
                runBlocking {
                    imageRepository.findFormat(
                        expectedImageFormat.imageGroup,
                        expectedImageFormat.imageName,
                        expectedImageFormat.versionName,
                        expectedImageFormat.type
                    )
                }
            } returns null

            every {
                runBlocking {
                    imageFileManager.write(
                        expectedImageFormat.imageGroup,
                        expectedImageFormat.imageName,
                        expectedImageFormat.versionName,
                        expectedImageFormat.type,
                        imageInput
                    )
                }
            } returns Qcow2ImageFile

            every {
                runBlocking {
                    imageRepository.findVersion(
                        expectedImageFormat.imageGroup,
                        expectedImageFormat.imageName,
                        expectedImageFormat.versionName
                    )
                }
            } returns null

            every {
                runBlocking {
                    imageRepository.find(
                        expectedImageFormat.imageGroup,
                        expectedImageFormat.imageName
                    )
                }
            } returns expectedImageFormat.image

            val versionSlot = slot<ImageVersion>()
            val formatSlot = slot<ImageFormat>()
            every { runBlocking { imageRepository.createVersion(capture(versionSlot)) } } answers {
                versionSlot.captured
            }
            every { runBlocking { imageRepository.createFormat(capture(formatSlot)) } } answers { formatSlot.captured }

            val format = handler.handleUpload(
                expectedImageFormat.image.ownerId,
                expectedImageFormat.imageGroup,
                expectedImageFormat.imageName,
                expectedImageFormat.versionName,
                Qcow2ImageFile.name,
                imageInput
            )
            versionSlot.captured shouldBe expectedImageFormat.version.copy(id = versionSlot.captured.id)
            format shouldBe ImageFormat1_1.copy(
                id = formatSlot.captured.id,
                version = versionSlot.captured,
                creationDate = clock.instant().atZone(clock.zone)
            )
        }

        @Test
        fun `should create image, version and return created image format`() = runBlocking {
            val expectedImageFormat = ImageFormat1_1
            val imageInput = Qcow2ImageFile.inputStream()

            every {
                runBlocking {
                    imageRepository.findFormat(
                        expectedImageFormat.imageGroup,
                        expectedImageFormat.imageName,
                        expectedImageFormat.versionName,
                        expectedImageFormat.type
                    )
                }
            } returns null

            every {
                runBlocking {
                    imageFileManager.write(
                        expectedImageFormat.imageGroup,
                        expectedImageFormat.imageName,
                        expectedImageFormat.versionName,
                        expectedImageFormat.type,
                        imageInput
                    )
                }
            } returns Qcow2ImageFile

            every {
                runBlocking {
                    imageRepository.findVersion(
                        expectedImageFormat.imageGroup,
                        expectedImageFormat.imageName,
                        expectedImageFormat.versionName
                    )
                }
            } returns null

            every {
                runBlocking {
                    imageRepository.find(
                        expectedImageFormat.imageGroup,
                        expectedImageFormat.imageName
                    )
                }
            } returns null

            val imageSlot = slot<Image>()
            val versionSlot = slot<ImageVersion>()
            val formatSlot = slot<ImageFormat>()
            every { runBlocking { imageRepository.create(capture(imageSlot)) } } answers { imageSlot.captured }
            every { runBlocking { imageRepository.createVersion(capture(versionSlot)) } } answers {
                versionSlot.captured
            }
            every { runBlocking { imageRepository.createFormat(capture(formatSlot)) } } answers { formatSlot.captured }

            val format = handler.handleUpload(
                expectedImageFormat.image.ownerId,
                expectedImageFormat.imageGroup,
                expectedImageFormat.imageName,
                expectedImageFormat.versionName,
                Qcow2ImageFile.name,
                imageInput
            )
            imageSlot.captured shouldBe expectedImageFormat.image.copy(
                id = imageSlot.captured.id,
                creationDate = clock.instant().atZone(clock.zone)
            )
            versionSlot.captured shouldBe expectedImageFormat.version.copy(
                id = versionSlot.captured.id,
                image = imageSlot.captured
            )
            format shouldBe ImageFormat1_1.copy(
                id = formatSlot.captured.id,
                version = versionSlot.captured,
                creationDate = clock.instant().atZone(clock.zone)
            )
        }
    }
}
