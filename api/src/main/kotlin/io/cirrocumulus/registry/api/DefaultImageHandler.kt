package io.cirrocumulus.registry.api

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.readAllParts
import io.ktor.request.isMultipart
import io.ktor.request.receiveMultipart
import io.ktor.response.respond

class DefaultImageHandler : ImageHandler {
    companion object {
        val AllowedFileContentTypes = setOf(ContentType.Application.OctetStream)
        const val FileParameter = "file"
    }

    override suspend fun handleUpload(call: ApplicationCall) {
        val part = findFilePart(call)
        call.respond(HttpStatusCode.OK)
    }

    private suspend fun findFilePart(call: ApplicationCall): PartData {
        if (!call.request.isMultipart()) {
            throw InvalidRequestContentTypeException(ContentType.MultiPart.FormData)
        }
        val part = call.receiveMultipart()
            .readAllParts()
            .firstOrNull { it.name == FileParameter }
            ?: throw MissingParameterException(FileParameter)
        if (!AllowedFileContentTypes.contains(part.contentType)) {
            throw InvalidFileContentTypeException(FileParameter, AllowedFileContentTypes)
        }
        return part
    }
}
