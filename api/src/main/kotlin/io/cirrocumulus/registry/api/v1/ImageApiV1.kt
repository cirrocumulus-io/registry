package io.cirrocumulus.registry.api.v1

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.routing.route

fun Routing.image(): Route = route("/v1") {
    authenticate("user") {
        post("/{name}/{version}") {
            call.respond(HttpStatusCode.OK)
        }
    }
}
