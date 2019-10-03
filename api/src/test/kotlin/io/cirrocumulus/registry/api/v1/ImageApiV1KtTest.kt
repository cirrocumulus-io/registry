package io.cirrocumulus.registry.api.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.cirrocumulus.registry.api.*
import io.cirrocumulus.registry.dto.*
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.ktor.http.*
import io.ktor.http.content.PartData
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.InternalAPI
import io.ktor.util.encodeBase64
import kotlinx.io.streams.asInput
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@InternalAPI
@Suppress("ClassName")
class ImageApiV1KtTest {
    val mapper = ObjectMapper().findAndRegisterModules()

    @Nested
    inner class upload {
        @Test
        fun `unauthorized when no authorization header`() = withTestApplication({ module(DbClient, Config) }) {
            with(handleRequest(HttpMethod.Post, "/v1/debian/9.0")) {
                response.status() shouldBe HttpStatusCode.Unauthorized
                response.content.shouldBeNull()
            }
        }

        @Test
        fun `unauthorized when wrong credentials`() = withTestApplication({ module(DbClient, Config) }) {
            val request = handleRequest(HttpMethod.Post, "/v1/debian/9.0") {
                addHeader(HttpHeaders.Authorization, "Basic ${"admin:change".encodeBase64()}")
            }
            with(request) {
                response.status() shouldBe HttpStatusCode.Unauthorized
                response.content.shouldBeNull()
            }
        }

        @Test
        fun `bad request when wrong content type header`() = withTestApplication({ module(DbClient, Config) }) {
            val request = handleRequest(HttpMethod.Post, "/v1/debian/9.0") {
                addHeader(HttpHeaders.Authorization, User1.basicAuthentication())
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
            with(request) {
                response.status() shouldBe HttpStatusCode.BadRequest
                response.content.should { body ->
                    val dto = InvalidRequestContentTypeErrorDto(ContentType.MultiPart.FormData.toString())
                    body.shouldNotBeNull()
                    mapper.readValue<ErrorDto>(body) shouldBe dto
                }
            }
        }

        @Test
        fun `bad request when no file parameter value`() = withTestApplication({ module(DbClient, Config) }) {
            val request = handleRequest(HttpMethod.Post, "/v1/debian/9.0") {
                addHeader(HttpHeaders.Authorization, User1.basicAuthentication())
                fillBodyWithTestFile(ContentType.Application.OctetStream, "parameter", Qcow2ImageFile.name)
            }
            with(request) {
                response.status() shouldBe HttpStatusCode.BadRequest
                response.content.should { body ->
                    val dto = MissingParameterErrorDto(FileParameter)
                    body.shouldNotBeNull()
                    mapper.readValue<ErrorDto>(body) shouldBe dto
                }
            }
        }

        @Test
        fun `bad request when wrong file content type`() = withTestApplication({ module(DbClient, Config) }) {
            val request = handleRequest(HttpMethod.Post, "/v1/debian/9.0") {
                addHeader(HttpHeaders.Authorization, User1.basicAuthentication())
                fillBodyWithTestFile(ContentType.Application.Json, "file", Qcow2ImageFile.name)
            }
            with(request) {
                response.status() shouldBe HttpStatusCode.BadRequest
                response.content.should { body ->
                    val dto = InvalidFileContentTypeErrorDto(
                        FileParameter,
                        AllowedFileContentTypes.map { it.toString() }.toSet()
                    )
                    body.shouldNotBeNull()
                    mapper.readValue<ErrorDto>(body) shouldBe dto
                }
            }
        }

        @Test
        fun `bad request when wrong file format`() = withTestApplication({ module(DbClient, Config) }) {
            val request = handleRequest(HttpMethod.Post, "/v1/debian/9.0") {
                addHeader(HttpHeaders.Authorization, User1.basicAuthentication())
                fillBodyWithTestFile(ContentType.Application.OctetStream, "file", "test.txt")
            }
            with(request) {
                response.status() shouldBe HttpStatusCode.BadRequest
                response.content.should { body ->
                    val dto = InvalidFileFormatErrorDto(FileParameter, DefaultImageHandler.AllowedFileExtensions)
                    body.shouldNotBeNull()
                    mapper.readValue<ErrorDto>(body) shouldBe dto
                }
            }
        }

        @Test
        fun `conflict when image format already exists`() = withTestApplication({ module(DbClient, Config) }) {
            val request = handleRequest(HttpMethod.Post, "/v1/debian/9.0") {
                addHeader(HttpHeaders.Authorization, User1.basicAuthentication())
                fillBodyWithTestFile(ContentType.Application.OctetStream, "file", Qcow2ImageFile.name)
            }
            with(request) {
                response.status() shouldBe HttpStatusCode.Conflict
                response.headers.should { headers ->
                    headers[HttpHeaders.Location] shouldBe "${Config.registry.baseUrl}/${ImageFormat1_1.uri}"
                }
                response.content.should { body ->
                    val dto = ImageFormatAlreadyExistsErrorDto
                    body.shouldNotBeNull()
                    mapper.readValue<ErrorDto>(body) shouldBe dto
                }
            }
        }

        @Test
        fun `created when image format is created`() = withTestApplication({ module(DbClient, Config) }) {
            val request = handleRequest(HttpMethod.Post, "/v1/fedora/30") {
                addHeader(HttpHeaders.Authorization, User1.basicAuthentication())
                fillBodyWithTestFile(ContentType.Application.OctetStream, "file", Qcow2ImageFile.name)
            }
            with(request) {
                response.status() shouldBe HttpStatusCode.Created
                response.headers.should { headers ->
                    val location = "${Config.registry.baseUrl}/v1/${User1.username}/fedora/30/qcow2"
                    headers[HttpHeaders.Location] shouldBe location
                }
                response.content.shouldBeNull()
            }
        }

        fun TestApplicationRequest.fillBodyWithTestFile(
            contentType: ContentType,
            parameter: String,
            filename: String?
        ) {
            val boundary = "boundary"
            addHeader(
                HttpHeaders.ContentType,
                ContentType.MultiPart.FormData.withParameter("boundary", boundary).toString()
            )
            setBody(
                boundary,
                listOf(
                    PartData.FileItem(
                        { Qcow2ImageFile.inputStream().asInput() },
                        {},
                        headersOf(
                            Pair(
                                HttpHeaders.ContentDisposition,
                                listOf(
                                    ContentDisposition.File
                                        .withParameter(ContentDisposition.Parameters.Name, parameter)
                                        .withParameter(ContentDisposition.Parameters.FileName, filename!!)
                                        .toString()
                                )
                            ),
                            Pair(
                                HttpHeaders.ContentType,
                                listOf(contentType.toString())
                            )
                        )
                    )
                )
            )
        }
    }
}
