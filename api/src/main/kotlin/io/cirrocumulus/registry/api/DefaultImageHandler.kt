package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.ImageFormat
import java.io.File
import java.io.InputStream

class DefaultImageHandler(
    private val imageRepository: ImageRepository
) : ImageHandler {
    companion object {
        val AllowedFileExtensions = setOf("qcow2")
    }

    override suspend fun handleUpload(
        group: String,
        name: String,
        version: String,
        imageOriginalFilename: String,
        imageInput: InputStream
    ): ImageFormat {
        val imageFileExtension = File(imageOriginalFilename).extension
        val formatType = imageFileExtension.toFormatType()
        val imageFormat = imageRepository.find(group, name, version, formatType)
        if (imageFormat != null) {
            throw ImageFormatAlreadyExistsException(imageFormat)
        }
        TODO()
    }

    private fun String.toFormatType(): ImageFormat.Type = when (this) {
        "qcow2" -> ImageFormat.Type.Qcow2
        else -> throw UnsupportedFormatException("${this} is not a supported format")
    }
}
