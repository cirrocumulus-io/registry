package io.cirrocumulus.registry.api.v1

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.cirrocumulus.registry.api.*
import io.cirrocumulus.registry.core.Image
import io.cirrocumulus.registry.core.ImageFormat
import io.cirrocumulus.registry.core.ImageVersion
import io.cirrocumulus.registry.core.User
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
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.io.streams.asInput
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URI

@InternalAPI
@Suppress("ClassName")
class ImageApiV1KtTest {
    val qcow2File = File(DefaultImageHandler::class.java.getResource("/test.qcow2").file)
    val config = Configuration()
    val mapper = ObjectMapper().findAndRegisterModules()

    val user = User(
        username = "user1",
        password = "\$2a\$12\$21SFFJSkRWjAeFt21v5mOe6lzDb7bvDfgcBVG66UB6/2mBYv8xOxS"
    )

    lateinit var imageHandler: ImageHandler
    lateinit var userRepository: UserRepository

    @BeforeEach
    fun beforeEach() {
        imageHandler = mockk()
        userRepository = mockk()
    }

    @Nested
    inner class upload {
        @Test
        fun `unauthorized when no authorization header`() = withTestApplication(
            { module(imageHandler, userRepository, config.registry) }
        ) {
            with(handleRequest(HttpMethod.Post, "/v1/debian/9.0")) {
                response.status() shouldBe HttpStatusCode.Unauthorized
                response.content.shouldBeNull()
            }
        }

        @Test
        fun `unauthorized when wrong credentials`() = withTestApplication(
            { module(imageHandler, userRepository, config.registry) }
        ) {
            val username = "admin"
            val password = "changeit"
            coEvery { userRepository.findByCredentials(username, password) } returns null

            val request = handleRequest(HttpMethod.Post, "/v1/debian/9.0") {
                addHeader(HttpHeaders.Authorization, "Basic ${"$username:$password".encodeBase64()}")
            }
            with(request) {
                response.status() shouldBe HttpStatusCode.Unauthorized
                response.content.shouldBeNull()
            }
        }

        @Test
        fun `bad request when wrong content type header`() = withTestApplication(
            { module(imageHandler, userRepository, config.registry) }
        ) {
            coEvery { userRepository.findByCredentials(user.username, user.password) } returns user

            val request = handleRequest(HttpMethod.Post, "/v1/debian/9.0") {
                addHeader(HttpHeaders.Authorization, "Basic ${"${user.username}:${user.password}".encodeBase64()}")
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
        fun `bad request when no file parameter value`() = withTestApplication(
            { module(imageHandler, userRepository, config.registry) }
        ) {
            coEvery { userRepository.findByCredentials(user.username, user.password) } returns user

            val request = handleRequest(HttpMethod.Post, "/v1/debian/9.0") {
                addHeader(HttpHeaders.Authorization, "Basic ${"${user.username}:${user.password}".encodeBase64()}")
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
        fun `bad request when wrong file content type`() = withTestApplication(
            { module(imageHandler, userRepository, config.registry) }
        ) {
            coEvery { userRepository.findByCredentials(user.username, user.password) } returns user

            val request = handleRequest(HttpMethod.Post, "/v1/debian/9.0") {
                addHeader(HttpHeaders.Authorization, "Basic ${"${user.username}:${user.password}".encodeBase64()}")
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
        fun `bad request when wrong file format`() = withTestApplication(
            { module(imageHandler, userRepository, config.registry) }
        ) {
            coEvery { userRepository.findByCredentials(user.username, user.password) } returns user

            val request = handleRequest(HttpMethod.Post, "/v1/debian/9.0") {
                addHeader(HttpHeaders.Authorization, "Basic ${"${user.username}:${user.password}".encodeBase64()}")
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
        fun `conflict when image format already exists`() = withTestApplication(
            { module(imageHandler, userRepository, config.registry) }
        ) {
            val format = ImageFormat(
                version = ImageVersion(
                    image = Image(
                        ownerId = user.id,
                        group = user.username,
                        name = "debian"
                    ),
                    name = "9.0"
                ),
                type = ImageFormat.Type.Qcow2,
                uri = URI("/v1/user1/debian/9.0/qcow2"),
                sha512 = "b5ec5e13aebc7eee4b0b6f2352225a99f23dbdd4317c2cb79e786d3ebb4a1b4984fdc444ee95862f976e645f0667e64380acc4f1a77d47502097d572a42f592a"
            )
            coEvery { userRepository.findByCredentials(user.username, user.password) } returns user
            coEvery {
                imageHandler.handleUpload(
                    user.id,
                    user.username,
                    format.imageName,
                    format.versionName,
                    "test.qcow2",
                    any()
                )
            } throws ImageFormatAlreadyExistsException(format)

            val request = handleRequest(HttpMethod.Post, "/v1/${format.imageName}/${format.versionName}") {
                addHeader(HttpHeaders.Authorization, "Basic ${"${user.username}:${user.password}".encodeBase64()}")
                fillBodyWithTestFile(ContentType.Application.OctetStream, "file", "test.qcow2")
            }
            with(request) {
                response.status() shouldBe HttpStatusCode.Conflict
                response.headers.should { headers ->
                    headers[HttpHeaders.Location] shouldBe "${config.registry.baseUrl}/${format.uri}"
                }
                response.content.should { body ->
                    val dto = ImageFormatAlreadyExistsErrorDto
                    body.shouldNotBeNull()
                    mapper.readValue<ErrorDto>(body) shouldBe dto
                }
            }
        }

        @Test
        fun `created when image format is created`() = withTestApplication(
            { module(imageHandler, userRepository, config.registry) }
        ) {
            val format = ImageFormat(
                version = ImageVersion(
                    image = Image(
                        ownerId = user.id,
                        group = user.username,
                        name = "debian"
                    ),
                    name = "9.0"
                ),
                type = ImageFormat.Type.Qcow2,
                uri = URI("/v1/user1/debian/9.0/qcow2"),
                sha512 = "b5ec5e13aebc7eee4b0b6f2352225a99f23dbdd4317c2cb79e786d3ebb4a1b4984fdc444ee95862f976e645f0667e64380acc4f1a77d47502097d572a42f592a"
            )
            coEvery { userRepository.findByCredentials(user.username, user.password) } returns user
            coEvery {
                imageHandler.handleUpload(
                    format.ownerId,
                    format.imageGroup,
                    format.imageName,
                    format.versionName,
                    "test.qcow2",
                    any()
                )
            } returns format

            val request = handleRequest(HttpMethod.Post, "/v1/${format.imageName}/${format.versionName}") {
                addHeader(HttpHeaders.Authorization, "Basic ${"${user.username}:${user.password}".encodeBase64()}")
                fillBodyWithTestFile(ContentType.Application.OctetStream, "file", "test.qcow2")
            }
            with(request) {
                response.status() shouldBe HttpStatusCode.Created
                response.headers.should { headers ->
                    val location = "${config.registry.baseUrl}${format.uri}"
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
                        { qcow2File.inputStream().asInput() },
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
