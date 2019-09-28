package io.cirrocumulus.registry.api.v1

import io.cirrocumulus.registry.api.DefaultImageHandler
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.routing.Route
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.routing.route

fun Routing.image(): Route = route("/v1") {
    val handler = DefaultImageHandler()

    authenticate("user") {
        post("/{name}/{version}") { handler.handleUpload(call) }
    }
}
