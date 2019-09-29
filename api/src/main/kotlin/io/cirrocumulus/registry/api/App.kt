package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.api.v1.image
import io.cirrocumulus.registry.dto.InvalidFileContentTypeErrorDto
import io.cirrocumulus.registry.dto.InvalidFileFormatErrorDto
import io.cirrocumulus.registry.dto.InvalidRequestContentTypeErrorDto
import io.cirrocumulus.registry.dto.MissingParameterErrorDto
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.basic
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080, module = Application::module).start(true)
}

fun Application.module() {
    val dbClient = Configuration.Database(
        username = "cirrocumulus_registry",
        password = "cirrocumulus_registry"
    ).createClient()
    val imageRepository = ImageR2dbcRepository(dbClient)
    val userRepository = UserR2dbcRepository(dbClient)

    install(Authentication) {
        basic(name = "user") {
            realm = "Cirrocumulus Registry"
            validate { (name1, password) ->
                userRepository
                    .findByCredentials(name1, password)
                    ?.let { UserIdPrincipal(it.username) }
            }
        }
    }

    install(ContentNegotiation) {
        jackson()
    }

    install(StatusPages) {
        exception<InvalidRequestException> { exception ->
            val dto = when (exception) {
                is InvalidFileContentTypeException -> exception.toDto()
                is InvalidFileFormatException -> exception.toDto()
                is InvalidRequestContentTypeException -> exception.toDto()
                is MissingParameterException -> exception.toDto()
            }
            call.respond(HttpStatusCode.BadRequest, dto)
        }
    }

    routing {
        image(imageRepository)
    }
}

private fun InvalidFileContentTypeException.toDto() = InvalidFileContentTypeErrorDto(
    parameter,
    allowedContentTypes.map { it.toString() }.toSet()
)

private fun InvalidFileFormatException.toDto() = InvalidFileFormatErrorDto(parameter, allowedFileFormats)

private fun InvalidRequestContentTypeException.toDto() = InvalidRequestContentTypeErrorDto(
    expectedContentType.toString()
)

private fun MissingParameterException.toDto() = MissingParameterErrorDto(parameter)
