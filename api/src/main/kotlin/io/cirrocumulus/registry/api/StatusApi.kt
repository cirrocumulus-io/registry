package io.cirrocumulus.registry.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import io.cirrocumulus.registry.dto.StatusDto
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route

fun Routing.statusApi() = route("/status") {
    get {
        val mapper = ObjectMapper(YAMLFactory()).findAndRegisterModules()
        val metadata = mapper.readValue<Metadata>(javaClass.getResourceAsStream("/metadata.yml"))
        call.respond(HttpStatusCode.OK, metadata.toStatusDto())
    }
}

private fun Metadata.toStatusDto() = StatusDto(
    version = version,
    build = build
)
