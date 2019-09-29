package io.cirrocumulus.registry.api.v1

import io.cirrocumulus.registry.api.*
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.http.ContentType
import io.ktor.http.content.PartData
import io.ktor.http.content.readAllParts
import io.ktor.request.isMultipart
import io.ktor.request.receiveMultipart
import io.ktor.routing.Route
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.pipeline.PipelineContext
import java.io.File

val AllowedFileContentTypes = setOf(ContentType.Application.OctetStream)
val AllowedFileExtensions = setOf("qcow2")

const val FileParameter = "file"

const val NamePathParameter = "name"
const val VersionPathParameter = "version"

fun Routing.image(imageRepository: ImageRepository): Route = route("/v1") {
    val handler = DefaultImageHandler(imageRepository)

    authenticate("user") {
        post("/{name}/{version}") {
            val part = findFilePart()

            val group = call.principal<UserIdPrincipal>()!!.name
            val name = call.parameters[NamePathParameter]!!
            val version = call.parameters[VersionPathParameter]!!
            handler.handleUpload(group, name, version)
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.findFilePart(): PartData.FileItem {
    if (!call.request.isMultipart()) {
        throw InvalidRequestContentTypeException(ContentType.MultiPart.FormData)
    }
    val part = call.receiveMultipart()
        .readAllParts()
        .filterIsInstance<PartData.FileItem>()
        .firstOrNull { it.name == FileParameter }
        ?: throw MissingParameterException(FileParameter)
    if (!AllowedFileContentTypes.contains(part.contentType)) {
        throw InvalidFileContentTypeException(FileParameter, AllowedFileContentTypes)
    }
    val fileExtension = part.contentDisposition?.name?.let { File(it).extension }
    if (!AllowedFileExtensions.contains(fileExtension)) {
        throw InvalidFileFormatException(FileParameter, AllowedFileExtensions)
    }
    return part
}
