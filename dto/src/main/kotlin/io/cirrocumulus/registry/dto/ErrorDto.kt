package io.cirrocumulus.registry.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

private const val ImageFormatAlreadyExistsCodeValue = "image_format_already_exists"
private const val InvalidFileContentTypeCodeValue = "invalid_file_content_type"
private const val InvalidFileFormatCodeValue = "invalid_file_format"
private const val InvalidRequestContentTypeCodeValue = "invalid_request_content_type"
private const val MalformedRequestCodeValue = "malformed_request"
private const val MissingParameterCodeValue = "missing_parameter"

@JsonTypeInfo(
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    use = JsonTypeInfo.Id.NAME,
    property = "code"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ImageFormatAlreadyExistsErrorDto::class, name = ImageFormatAlreadyExistsCodeValue),
    JsonSubTypes.Type(value = InvalidFileContentTypeErrorDto::class, name = InvalidFileContentTypeCodeValue),
    JsonSubTypes.Type(value = InvalidFileFormatErrorDto::class, name = InvalidFileFormatCodeValue),
    JsonSubTypes.Type(value = InvalidRequestContentTypeErrorDto::class, name = InvalidRequestContentTypeCodeValue),
    JsonSubTypes.Type(value = MissingParameterErrorDto::class, name = MissingParameterCodeValue)
)
sealed class ErrorDto {
    enum class Code {
        @JsonProperty(ImageFormatAlreadyExistsCodeValue)
        ImageFormatAlreadyExists,

        @JsonProperty(InvalidFileContentTypeCodeValue)
        InvalidFileContentType,

        @JsonProperty(InvalidFileFormatCodeValue)
        InvalidFileFormat,

        @JsonProperty(InvalidRequestContentTypeCodeValue)
        InvalidRequestContentType,

        @JsonProperty(MalformedRequestCodeValue)
        MalformedRequest,

        @JsonProperty(MissingParameterCodeValue)
        MissingParameter
    }

    abstract val code: Code
}

sealed class InvalidParameterErrorDto : ErrorDto() {
    abstract val parameter: String
}

object ImageFormatAlreadyExistsErrorDto : ErrorDto() {
    override val code = Code.ImageFormatAlreadyExists

    override fun equals(other: Any?): Boolean = other is ImageFormatAlreadyExistsErrorDto
}

data class InvalidFileContentTypeErrorDto(
    override val parameter: String,
    val allowedContentTypes: Set<String>
) : InvalidParameterErrorDto() {
    override val code = Code.InvalidFileContentType
}

data class InvalidFileFormatErrorDto(
    override val parameter: String,
    val allowedFileFormats: Set<String>
) : InvalidParameterErrorDto() {
    override val code = Code.InvalidFileFormat
}

data class InvalidRequestContentTypeErrorDto(
    val expectedContentType: String
) : ErrorDto() {
    override val code = Code.InvalidRequestContentType
}

data class MissingParameterErrorDto(
    override val parameter: String
) : InvalidParameterErrorDto() {
    override val code = Code.MissingParameter
}
