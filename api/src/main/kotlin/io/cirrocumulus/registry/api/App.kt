package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.api.v1.image
import io.cirrocumulus.registry.dto.*
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.basic
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.r2dbc.client.R2dbc

fun main(args: Array<String>) {
    val config = Configuration()
    val dbClient = config.db.createClient()
    embeddedServer(Netty, 8080) { module(dbClient, config) }.start(true)
}

fun Application.module(dbClient: R2dbc, config: Configuration) {
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
            when (exception) {
                is ImageFormatAlreadyExistsException -> exception.send(call, config)
                is InvalidFileContentTypeException -> exception.send(call)
                is InvalidFileFormatException -> exception.send(call)
                is InvalidRequestContentTypeException -> exception.send(call)
                is MissingParameterException -> exception.send(call)
            }
        }
    }

    routing {
        image(imageRepository)
    }
}

private suspend fun ImageFormatAlreadyExistsException.send(call: ApplicationCall, config: Configuration) {
    call.response.headers.append(HttpHeaders.Location, "${config.registry.baseUrl}/${imageFormat.uri}")
    call.respond(HttpStatusCode.Conflict, ImageFormatAlreadyExistsErrorDto)
}

private suspend fun InvalidFileContentTypeException.send(call: ApplicationCall) {
    val dto = InvalidFileContentTypeErrorDto(parameter, allowedContentTypes.map { it.toString() }.toSet())
    call.respond(HttpStatusCode.BadRequest, dto)
}

private suspend fun InvalidFileFormatException.send(call: ApplicationCall) {
    val dto = InvalidFileFormatErrorDto(parameter, allowedFileFormats)
    call.respond(HttpStatusCode.BadRequest, dto)
}

private suspend fun InvalidRequestContentTypeException.send(call: ApplicationCall) {
    val dto = InvalidRequestContentTypeErrorDto(expectedContentType.toString())
    call.respond(HttpStatusCode.BadRequest, dto)
}

private suspend fun MissingParameterException.send(call: ApplicationCall) {
    val dto = MissingParameterErrorDto(parameter)
    call.respond(HttpStatusCode.BadRequest, dto)
}
