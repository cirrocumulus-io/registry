package io.cirrocumulus.registry.api.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.cirrocumulus.registry.api.DbClient
import io.cirrocumulus.registry.api.module
import io.cirrocumulus.registry.dto.ErrorDto
import io.cirrocumulus.registry.dto.InvalidFileContentTypeErrorDto
import io.cirrocumulus.registry.dto.InvalidRequestContentTypeErrorDto
import io.cirrocumulus.registry.dto.MissingParameterErrorDto
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
        fun `unauthorized with no authorization header`() = withTestApplication({ module(DbClient) }) {
            with(handleRequest(HttpMethod.Post, "/v1/debian/9.0")) {
                response.status() shouldBe HttpStatusCode.Unauthorized
                response.content.shouldBeNull()
            }
        }

        @Test
        fun `unauthorized with wrong credentials`() = withTestApplication({ module(DbClient) }) {
            val request = handleRequest(HttpMethod.Post, "/v1/debian/9.0") {
                addHeader(HttpHeaders.Authorization, "Basic ${"admin:change".encodeBase64()}")
            }
            with(request) {
                response.status() shouldBe HttpStatusCode.Unauthorized
                response.content.shouldBeNull()
            }
        }

        @Test
        fun `bad request with wrong content type header`() = withTestApplication({ module(DbClient) }) {
            val request = handleRequest(HttpMethod.Post, "/v1/debian/9.0") {
                addHeader(HttpHeaders.Authorization, "Basic ${"admin:changeit".encodeBase64()}")
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
        fun `bad request with no file parameter value`() = withTestApplication({ module(DbClient) }) {
            val request = handleRequest(HttpMethod.Post, "/v1/debian/9.0") {
                addHeader(HttpHeaders.Authorization, "Basic ${"admin:changeit".encodeBase64()}")
                fillBodyWithTestFile(ContentType.Application.OctetStream, "parameter", "test.qcow2")
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
        fun `bad request with wrong file content type`() = withTestApplication({ module(DbClient) }) {
            val request = handleRequest(HttpMethod.Post, "/v1/debian/9.0") {
                addHeader(HttpHeaders.Authorization, "Basic ${"admin:changeit".encodeBase64()}")
                fillBodyWithTestFile(ContentType.Application.Json, "file", "test.qcow2")
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
        fun `bad request with wrong file format`() = withTestApplication({ module(DbClient) }) {
            val request = handleRequest(HttpMethod.Post, "/v1/debian/9.0") {
                addHeader(HttpHeaders.Authorization, "Basic ${"admin:changeit".encodeBase64()}")
                fillBodyWithTestFile(ContentType.Application.Json, "file", "test.txt")
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
                        { javaClass.getResourceAsStream("/test.qcow2").asInput() },
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
