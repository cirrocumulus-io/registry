package io.cirrocumulus.registry.api.v1

import io.cirrocumulus.registry.api.module
import io.kotlintest.shouldBe
import io.ktor.application.Application
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.InternalAPI
import io.ktor.util.encodeBase64
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@InternalAPI
@Suppress("ClassName")
class ImageApiV1KtTest {
    @Nested
    inner class upload {
        @Test
        fun `unauthorized with no authorization header`() = withTestApplication(Application::module) {
            with(handleRequest(HttpMethod.Post, "/v1/debian/9.0")) {
                response.status() shouldBe HttpStatusCode.Unauthorized
                response.content shouldBe null
            }
        }

        @Test
        fun `unauthorized with wrong credentials`() = withTestApplication(Application::module) {
            val request = handleRequest(HttpMethod.Post, "/v1/debian/9.0") {
                addHeader(HttpHeaders.Authorization, "Basic ${"admin:change".encodeBase64()}")
            }
            with(request) {
                response.status() shouldBe HttpStatusCode.Unauthorized
                response.content shouldBe null
            }
        }

        @Test
        fun ok() = withTestApplication(Application::module) {
            val request = handleRequest(HttpMethod.Post, "/v1/debian/9.0") {
                addHeader(HttpHeaders.Authorization, "Basic ${"admin:changeit".encodeBase64()}")
            }
            with(request) {
                response.status() shouldBe HttpStatusCode.OK
                response.content shouldBe null
            }
        }
    }
}
