package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.Image
import io.cirrocumulus.registry.core.ImageFormat
import io.cirrocumulus.registry.core.ImageVersion
import io.cirrocumulus.registry.core.User
import io.r2dbc.client.R2dbc
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import kotlinx.coroutines.reactive.awaitSingle

abstract class R2dbcRepositoryTest {
    companion object {
        val DbClient = R2dbc(
            ConnectionFactories.get(
                ConnectionFactoryOptions
                    .builder()
                    .option(Option.valueOf("driver"), "postgresql")
                    .option(Option.valueOf("protocol"), "postgresql")
                    .option(Option.valueOf("host"), "localhost")
                    .option(Option.valueOf("port"), 5432)
                    .option(Option.valueOf("database"), "cirrocumulus_registry")
                    .option(Option.valueOf("user"), "cirrocumulus_registry")
                    .option(Option.valueOf("password"), "cirrocumulus_registry")
                    .build()
            )
        )
    }

    suspend fun deleteImage(image: Image) {
        DbClient
            .inTransaction { handle ->
                handle
                    .createUpdate(
                        """
                        DELETE FROM "${ImageR2dbcRepository.ImageTable}"
                        WHERE ${ImageR2dbcRepository.ImageIdColumn} = $1
                        """.trimIndent()
                    )
                    .bind("$1", image.id)
                    .execute()
            }
            .awaitSingle()
    }

    suspend fun deleteUser(user: User) {
        DbClient
            .inTransaction { handle ->
                handle
                    .createUpdate(
                        """
                        DELETE FROM "${UserR2dbcRepository.UserTable}"
                        WHERE ${UserR2dbcRepository.IdColumn} = $1
                        """.trimIndent()
                    )
                    .bind("$1", user.id)
                    .execute()
            }
            .awaitSingle()
    }

    suspend fun insertFormat(format: ImageFormat) {
        DbClient
            .inTransaction { handle ->
                handle
                    .execute(
                        """
                        INSERT INTO ${ImageR2dbcRepository.FormatTable}
                        VALUES ($1, $2, $3::format_type, $4, $5, $6)
                        """.trimIndent(),
                        format.id,
                        format.version.id,
                        format.type.name.toLowerCase(),
                        format.uri,
                        format.sha512,
                        format.creationDate
                    )
                    .map { format }
            }
            .awaitSingle()
    }

    suspend fun insertImage(image: Image) {
        DbClient
            .inTransaction { handle ->
                handle
                    .createUpdate(
                        """
                        INSERT INTO ${ImageR2dbcRepository.ImageTable}
                        VALUES ($1, $2, $3, $4, $5)
                        """.trimIndent()
                    )
                    .bind("$1", image.id)
                    .bind("$2", image.ownerId)
                    .bind("$3", image.group)
                    .bind("$4", image.name)
                    .bind("$5", image.creationDate)
                    .execute()
                    .map { image }
            }
            .awaitSingle()
    }

    suspend fun insertUser(user: User) {
        DbClient
            .inTransaction { handle ->
                handle
                    .createUpdate(
                        """
                        INSERT INTO "${UserR2dbcRepository.UserTable}"
                        VALUES($1, $2, $3)
                        """.trimIndent()
                    )
                    .bind("$1", user.id)
                    .bind("$2", user.username)
                    .bind("$3", user.password)
                    .execute()
            }
            .awaitSingle()
    }

    suspend fun insertVersion(version: ImageVersion) {
        DbClient
            .inTransaction { handle ->
                handle
                    .execute(
                        """
                        INSERT INTO ${ImageR2dbcRepository.VersionTable}
                        VALUES ($1, $2, $3)
                        """.trimIndent(),
                        version.id,
                        version.image.id,
                        version.name
                    )
                    .map { version }
            }
            .awaitSingle()
    }
}
