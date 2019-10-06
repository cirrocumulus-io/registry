package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.Image
import io.cirrocumulus.registry.core.ImageFormat
import io.cirrocumulus.registry.core.ImageVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.digest.MessageDigestAlgorithms
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.net.URI
import java.time.Clock
import java.util.*

class DefaultImageHandler : ImageHandler {
    companion object {
        val AllowedFileExtensions = ImageFormat.Type.values().map { it.fileExtension }.toSet()
        private val DigestUtils = DigestUtils(MessageDigestAlgorithms.SHA3_512)
        private val Logger = LoggerFactory.getLogger(DefaultImageHandler::class.java)
    }

    private val imageRepository: ImageRepository
    private val imageFileManager: ImageFileManager
    private val clock: Clock

    constructor(imageRepository: ImageRepository, imageFileManager: ImageFileManager) : this(
        imageRepository, imageFileManager, Clock.systemDefaultZone()
    )

    internal constructor(imageRepository: ImageRepository, imageFileManager: ImageFileManager, clock: Clock) {
        this.imageRepository = imageRepository
        this.imageFileManager = imageFileManager
        this.clock = clock
    }

    override suspend fun handleUpload(
        userId: UUID,
        group: String,
        name: String,
        versionName: String,
        imageOriginalFilename: String,
        imageFileInput: InputStream
    ): ImageFormat {
        val imageFileExtension = File(imageOriginalFilename).extension
        val formatType = imageFileExtension.toFormatType()
        val existingImageFormat = imageRepository.findFormat(group, name, versionName, formatType)
        if (existingImageFormat != null) {
            throw ImageFormatAlreadyExistsException(existingImageFormat)
        }
        val file = imageFileManager.write(group, name, versionName, formatType, imageFileInput)
        try {
            return createImageFormat(userId, group, name, versionName, formatType, file.computeSha512())
        } catch (exception: Exception) {
            file.delete()
            Logger.debug("{} deleted due error", file)
            throw exception
        }
    }

    private suspend fun File.computeSha512(): String {
        val file = this
        return withContext(Dispatchers.IO) {
            DigestUtils.digestAsHex(file)
        }
    }

    private suspend fun createImage(userId: UUID, group: String, name: String) = imageRepository.create(
        Image(
            ownerId = userId,
            group = group,
            name = name,
            creationDate = clock.instant().atZone(clock.zone)
        )
    )

    private suspend fun createImageVersion(
        userId: UUID,
        group: String,
        name: String,
        versionName: String
    ) = imageRepository.createVersion(
        ImageVersion(
            image = imageRepository.find(group, name) ?: createImage(userId, group, name),
            name = versionName
        )
    )

    private suspend fun createImageFormat(
        userId: UUID,
        group: String,
        name: String,
        versionName: String,
        type: ImageFormat.Type,
        sha512: String
    ): ImageFormat {
        val format = imageRepository.createFormat(
            ImageFormat(
                version = imageRepository.findVersion(group, name, versionName) ?: createImageVersion(
                    userId,
                    group,
                    name,
                    versionName
                ),
                type = type,
                uri = URI("/v1/$group/$name/$versionName/${type.toString().toLowerCase()}"),
                sha512 = sha512,
                creationDate = clock.instant().atZone(clock.zone)
            )
        )
        Logger.info("New image format {}", format.uri)
        return format
    }

    private fun String.toFormatType(): ImageFormat.Type = ImageFormat.Type.values()
        .firstOrNull { it.fileExtension == this }
        ?: throw UnsupportedFormatException("$this is not a supported format")
}
