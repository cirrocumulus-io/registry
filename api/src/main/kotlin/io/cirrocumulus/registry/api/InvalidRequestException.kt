package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.ImageFormat
import io.ktor.http.ContentType

sealed class InvalidRequestException : Exception()

sealed class InvalidParameterException : InvalidRequestException() {
    abstract val parameter: String
}

data class ImageFormatAlreadyExistsException(
    val imageFormat: ImageFormat
) : InvalidRequestException()

class InvalidFileContentTypeException(
    override val parameter: String,
    val allowedContentTypes: Set<ContentType>
) : InvalidParameterException()

class InvalidFileFormatException(
    override val parameter: String,
    val allowedFileFormats: Set<String>
) : InvalidParameterException()

class InvalidRequestContentTypeException(
    val expectedContentType: ContentType
) : InvalidRequestException()

class MissingParameterException(
    override val parameter: String
) : InvalidParameterException()
