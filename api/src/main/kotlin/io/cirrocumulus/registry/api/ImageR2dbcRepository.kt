package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.ImageFormat
import io.r2dbc.client.R2dbc
import kotlinx.coroutines.reactive.awaitSingle

class ImageR2dbcRepository(
    private val dbClient: R2dbc
) : ImageRepository {
    internal companion object {
        const val ImageTable = "image"
        const val ImageIdColumn = "id"
        const val ImageGroupColumn = "group"
        const val ImageNameColumn = "name"

        const val VersionTable = "image_version"
        const val VersionIdColumn = "id"
        const val VersionImageIdColumn = "image_id"
        const val VersionNameColumn = "name"

        const val FormatTable = "image_format"
        const val FormatVersionIdColumn = "version_id"
        const val FormatTypeColumn = "type"
    }

    override suspend fun exists(group: String, name: String, version: String, formatType: ImageFormat.Type): Boolean =
        dbClient
            .inTransaction { handle ->
                handle
                    .select(
                        """
                        SELECT EXISTS(
                            SELECT 1
                            FROM $ImageTable i
                            JOIN $VersionTable v ON (v.$VersionImageIdColumn = i.$ImageIdColumn)
                            JOIN $FormatTable f ON (f.$FormatVersionIdColumn = v.$VersionIdColumn)
                            WHERE i.$ImageGroupColumn = $1 
                                AND i.$ImageNameColumn = $2
                                AND v.$VersionNameColumn = $3
                                AND f.$FormatTypeColumn = $4::format_type
                        )
                        """.trimIndent()
                    )
                    .bind("$1", group)
                    .bind("$2", name)
                    .bind("$3", version)
                    .bind("$4", formatType.toString())
                    .mapRow { row, _ -> row[0] as Boolean }
                    .single()
            }
            .awaitSingle()
}
