package io.cirrocumulus.registry.api

import io.ktor.http.ContentType

sealed class InvalidRequestException : Exception()

sealed class InvalidParameterException : InvalidRequestException() {
    abstract val parameter: String
}

class InvalidFileContentTypeException(
    override val parameter: String,
    val allowedContentTypes: Set<ContentType>
) : InvalidParameterException()

class InvalidRequestContentTypeException(
    val expectedContentType: ContentType
) : InvalidRequestException()

class MissingParameterException(
    override val parameter: String
) : InvalidParameterException()
