package io.cirrocumulus.registry.api

import io.ktor.application.ApplicationCall

interface ImageHandler {
    suspend fun handleUpload(call: ApplicationCall)
}
