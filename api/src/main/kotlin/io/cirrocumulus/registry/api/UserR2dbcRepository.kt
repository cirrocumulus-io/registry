package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.User
import io.r2dbc.client.R2dbc
import io.r2dbc.spi.Row
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class UserR2dbcRepository(
    private val dbClient: R2dbc
) : UserRepository {
    internal companion object {
        const val IdColumnName = "id"
        const val PasswordColumnName = "password"
        const val UserTable = "\"user\""
        const val UsernameColumnName = "username"
    }

    override suspend fun findByCredentials(username: String, password: String): User? = dbClient
        .inTransaction { handle ->
            handle
                .select("SELECT * FROM $UserTable WHERE $UsernameColumnName = $1")
                .bind("$1", username)
                .mapRow { row, _ -> row.toUser() }
                .singleOrEmpty()
                .filter { BCrypt.checkpw(password, it.password) }
        }
        .awaitFirstOrNull()

    private fun Row.toUser(): User = User(
        id = get(IdColumnName, UUID::class.java)!!,
        username = get(UsernameColumnName, String::class.java)!!,
        password = get(PasswordColumnName, String::class.java)!!
    )
}
