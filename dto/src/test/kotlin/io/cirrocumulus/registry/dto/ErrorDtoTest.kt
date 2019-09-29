package io.cirrocumulus.registry.dto

import org.junit.jupiter.api.Nested

class ErrorDtoTest {
    @Nested
    inner class InvalidFileContentType : JsonTest(
        "error/invalid-file-content-type",
        InvalidFileContentTypeErrorDto(
            "file",
            setOf("application/octet-stream")
        ),
        ErrorDto::class
    )

    @Nested
    inner class InvalidFileFormat : JsonTest(
        "error/invalid-file-format",
        InvalidFileFormatErrorDto(
            "file",
            setOf("qcow2")
        ),
        ErrorDto::class
    )

    @Nested
    inner class InvalidRequestContentType : JsonTest(
        "error/invalid-request-content-type",
        InvalidRequestContentTypeErrorDto("application/json"),
        ErrorDto::class
    )

    @Nested
    inner class MissingParameter : JsonTest(
        "error/missing-parameter",
        MissingParameterErrorDto("name"),
        ErrorDto::class
    )
}
