package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.api.v1.image
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.basic
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun Application.module() {
    val dbClient = Configuration.Database(
        username = "cirrocumulus_registry",
        password = "cirrocumulus_registry"
    ).createClient()
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

    routing {
        image()
    }
}

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080, module = Application::module).start(true)
}
