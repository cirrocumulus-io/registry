package io.cirrocumulus.registry.api

import io.kotlintest.shouldBe
import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AppKtTest {
    @Nested
    inner class Security {
        @Test
        fun unauthorized() = withTestApplication(Application::module) {
            arrayOf(
                handleRequest(HttpMethod.Post, "/v1/debian/9.0")
            ).forEach { request ->
                with(request) {
                    response.status() shouldBe HttpStatusCode.Unauthorized
                    response.content shouldBe null
                }
            }
        }
    }
}
