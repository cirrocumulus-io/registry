package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.Image
import io.cirrocumulus.registry.core.ImageFormat
import io.cirrocumulus.registry.core.ImageVersion
import java.io.File
import java.io.InputStream
import java.net.URI
import java.util.*

class DefaultImageHandler(
    private val imageRepository: ImageRepository
) : ImageHandler {
    companion object {
        val AllowedFileExtensions = setOf("qcow2")
    }

    override suspend fun handleUpload(
        userId: UUID,
        group: String,
        name: String,
        version: String,
        imageOriginalFilename: String,
        imageInput: InputStream
    ): ImageFormat {
        val imageFileExtension = File(imageOriginalFilename).extension
        val formatType = imageFileExtension.toFormatType()
        val existingImageFormat = imageRepository.findFormat(group, name, version, formatType)
        if (existingImageFormat != null) {
            throw ImageFormatAlreadyExistsException(existingImageFormat)
        }
        return createImageFormat(userId, group, name, version, formatType, "")
    }

    private suspend fun createImage(userId: UUID, group: String, name: String) = imageRepository.create(
        Image(
            ownerId = userId,
            group = group,
            name = name
        )
    )

    private suspend fun createImageVersion(userId: UUID, group: String, name: String, version: String) =
        imageRepository.createVersion(
            ImageVersion(
                image = imageRepository.find(group, name) ?: createImage(userId, group, name),
                name = version
            )
        )

    private suspend fun createImageFormat(
        userId: UUID,
        group: String,
        name: String,
        version: String,
        type: ImageFormat.Type,
        sha512: String
    ) = imageRepository.createFormat(
        ImageFormat(
            version = imageRepository.findVersion(group, name, version) ?: createImageVersion(
                userId,
                group,
                name,
                version
            ),
            type = type,
            uri = URI("/v1/$group/$name/$version/${type.toString().toLowerCase()}"),
            sha512 = sha512
        )
    )

    private fun String.toFormatType(): ImageFormat.Type = when (this) {
        "qcow2" -> ImageFormat.Type.Qcow2
        else -> throw UnsupportedFormatException("${this} is not a supported format")
    }
}
