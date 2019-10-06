package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.Image
import io.cirrocumulus.registry.core.ImageFormat
import io.cirrocumulus.registry.core.ImageVersion
import io.cirrocumulus.registry.core.User
import io.kotlintest.matchers.file.shouldNotExist
import io.kotlintest.matchers.throwable.shouldHaveMessage
import io.kotlintest.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.net.URI
import java.sql.SQLException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@Suppress("ClassName", "BlockingMethodInNonBlockingContext")
class DefaultImageHandlerTest {
    val qcow2File = File(DefaultImageHandler::class.java.getResource("/test.qcow2").file)
    val user = User(
        username = "user1",
        password = "\$2a\$12\$21SFFJSkRWjAeFt21v5mOe6lzDb7bvDfgcBVG66UB6/2mBYv8xOxS"
    )
    val format = ImageFormat(
        version = ImageVersion(
            image = Image(
                ownerId = user.id,
                group = user.username,
                name = "fedora"
            ),
            name = "30"
        ),
        type = ImageFormat.Type.Qcow2,
        uri = URI("/v1/user1/fedora/30/qcow2"),
        sha512 = "b5ec5e13aebc7eee4b0b6f2352225a99f23dbdd4317c2cb79e786d3ebb4a1b4984fdc444ee95862f976e645f0667e64380acc4f1a77d47502097d572a42f592a"
    )

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
                        user.id,
                        "group",
                        "name",
                        "version",
                        "test.txt",
                        qcow2File.inputStream()
                    )
                }
            }
            exception shouldHaveMessage "txt is not a supported format"
        }

        @Test
        fun `should fail if format already exists`() {
            coEvery {
                imageRepository.findFormat(
                    format.imageGroup,
                    format.imageName,
                    format.versionName,
                    ImageFormat.Type.Qcow2
                )
            } returns format

            val exception = assertThrows<ImageFormatAlreadyExistsException> {
                runBlocking {
                    handler.handleUpload(
                        format.ownerId,
                        format.imageGroup,
                        format.imageName,
                        format.versionName,
                        qcow2File.name,
                        qcow2File.inputStream()
                    )
                }
            }
            exception.format shouldBe format
        }

        @Test
        fun `should remove file if database error occurred`() = runBlocking {
            val imageInput = qcow2File.inputStream()
            val file = qcow2File.copyTo(File("${qcow2File.parent}/test-copy.qcow2"))
            file.createNewFile()

            coEvery {
                imageRepository.findFormat(format.imageGroup, format.imageName, format.versionName, format.type)
            } returns null

            coEvery {
                imageFileManager.write(format.imageGroup, format.imageName, format.versionName, format.type, imageInput)
            } returns file

            coEvery {
                imageRepository.findVersion(format.imageGroup, format.imageName, format.versionName)
            } returns format.version

            coEvery {
                imageRepository.find(format.imageGroup, format.imageName)
            } returns format.image

            val expectedException = SQLException()
            coEvery { imageRepository.createFormat(any()) } answers { throw expectedException }

            val exception = assertThrows<SQLException> {
                runBlocking {
                    handler.handleUpload(
                        format.image.ownerId,
                        format.imageGroup,
                        format.imageName,
                        format.versionName,
                        qcow2File.name,
                        imageInput
                    )
                }
            }
            exception shouldBe expectedException
            file.shouldNotExist()
        }

        @Test
        fun `should return created image format`() = runBlocking {
            val imageInput = qcow2File.inputStream()

            coEvery {
                imageRepository.findFormat(format.imageGroup, format.imageName, format.versionName, format.type)
            } returns null

            coEvery {
                imageFileManager.write(format.imageGroup, format.imageName, format.versionName, format.type, imageInput)
            } returns qcow2File

            coEvery {
                imageRepository.findVersion(format.imageGroup, format.imageName, format.versionName)
            } returns format.version

            coEvery {
                imageRepository.find(format.imageGroup, format.imageName)
            } returns format.image

            val formatSlot = slot<ImageFormat>()
            coEvery { imageRepository.createFormat(capture(formatSlot)) } answers { formatSlot.captured }

            handler
                .handleUpload(
                    format.image.ownerId,
                    format.imageGroup,
                    format.imageName,
                    format.versionName,
                    qcow2File.name,
                    imageInput
                )
                .shouldBe(format.copy(id = formatSlot.captured.id, creationDate = clock.instant().atZone(clock.zone)))
        }

        @Test
        fun `should create version and return created image format`() = runBlocking {
            val imageInput = qcow2File.inputStream()

            coEvery {
                imageRepository.findFormat(format.imageGroup, format.imageName, format.versionName, format.type)
            } returns null

            coEvery {
                imageFileManager.write(format.imageGroup, format.imageName, format.versionName, format.type, imageInput)
            } returns qcow2File

            coEvery {
                imageRepository.findVersion(format.imageGroup, format.imageName, format.versionName)
            } returns null

            coEvery { imageRepository.find(format.imageGroup, format.imageName) } returns format.image

            val versionSlot = slot<ImageVersion>()
            val formatSlot = slot<ImageFormat>()
            coEvery { imageRepository.createVersion(capture(versionSlot)) } answers { versionSlot.captured }
            coEvery { imageRepository.createFormat(capture(formatSlot)) } answers { formatSlot.captured }

            handler
                .handleUpload(
                    format.image.ownerId,
                    format.imageGroup,
                    format.imageName,
                    format.versionName,
                    qcow2File.name,
                    imageInput
                )
                .shouldBe(
                    format.copy(
                        id = formatSlot.captured.id,
                        version = versionSlot.captured,
                        creationDate = clock.instant().atZone(clock.zone)
                    )
                )
            versionSlot.captured shouldBe format.version.copy(id = versionSlot.captured.id)
        }

        @Test
        fun `should create image, version and return created image format`() = runBlocking {
            val imageInput = qcow2File.inputStream()

            coEvery {
                imageRepository.findFormat(format.imageGroup, format.imageName, format.versionName, format.type)
            } returns null

            coEvery {
                imageFileManager.write(format.imageGroup, format.imageName, format.versionName, format.type, imageInput)
            } returns qcow2File

            coEvery {
                imageRepository.findVersion(format.imageGroup, format.imageName, format.versionName)
            } returns null

            coEvery { imageRepository.find(format.imageGroup, format.imageName) } returns null

            val imageSlot = slot<Image>()
            val versionSlot = slot<ImageVersion>()
            val formatSlot = slot<ImageFormat>()
            coEvery { imageRepository.create(capture(imageSlot)) } answers { imageSlot.captured }
            coEvery { imageRepository.createVersion(capture(versionSlot)) } answers { versionSlot.captured }
            coEvery { imageRepository.createFormat(capture(formatSlot)) } answers { formatSlot.captured }

            handler
                .handleUpload(
                    format.image.ownerId,
                    format.imageGroup,
                    format.imageName,
                    format.versionName,
                    qcow2File.name,
                    imageInput
                ).shouldBe(
                    format.copy(
                        id = formatSlot.captured.id,
                        version = versionSlot.captured,
                        creationDate = clock.instant().atZone(clock.zone)
                    )
                )
            imageSlot.captured shouldBe format.image.copy(
                id = imageSlot.captured.id,
                creationDate = clock.instant().atZone(clock.zone)
            )
            versionSlot.captured shouldBe format.version.copy(
                id = versionSlot.captured.id,
                image = imageSlot.captured
            )
        }
    }
}
