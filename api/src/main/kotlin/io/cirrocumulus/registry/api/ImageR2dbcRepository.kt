package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.Image
import io.cirrocumulus.registry.core.ImageFormat
import io.cirrocumulus.registry.core.ImageVersion
import io.r2dbc.client.R2dbc
import io.r2dbc.spi.Row
import kotlinx.coroutines.reactive.awaitFirstOrNull
import java.net.URI
import java.time.OffsetDateTime
import java.util.*

class ImageR2dbcRepository(
    private val dbClient: R2dbc
) : ImageRepository {
    internal companion object {
        const val ImageTable = "image"
        const val ImageIdColumn = "id"
        const val ImageOwnerIdColumn = "owner_id"
        const val ImageGroupColumn = "group"
        const val ImageNameColumn = "name"
        const val ImageCreationDateColumn = "creation_date"

        const val VersionTable = "image_version"
        const val VersionIdColumn = "id"
        const val VersionImageIdColumn = "image_id"
        const val VersionNameColumn = "name"

        const val FormatTable = "image_format"
        const val FormatIdColumn = "id"
        const val FormatVersionIdColumn = "version_id"
        const val FormatTypeColumn = "type"
        const val FormatUriColumn = "uri"
        const val FormatSha512Column = "sha512"
        const val FormatCreationDateColumn = "creation_date"
    }

    override suspend fun find(
        group: String,
        name: String,
        version: String,
        formatType: ImageFormat.Type
    ): ImageFormat? = dbClient
        .inTransaction { handle ->
            handle
                .select(
                    """
                    SELECT 
                        i.$ImageIdColumn AS i_$ImageIdColumn,
                        i.$ImageOwnerIdColumn AS i_$ImageOwnerIdColumn,
                        i.$ImageGroupColumn AS i_$ImageGroupColumn,
                        i.$ImageNameColumn AS i_$ImageNameColumn,
                        i.$ImageCreationDateColumn AS i_$ImageCreationDateColumn,
                        v.$VersionIdColumn AS v_$VersionIdColumn,
                        v.$VersionNameColumn AS v_$VersionNameColumn,
                        f.$FormatIdColumn AS f_$FormatIdColumn,
                        f.$FormatTypeColumn::varchar AS f_$FormatTypeColumn,
                        f.$FormatUriColumn AS f_$FormatUriColumn,
                        f.$FormatSha512Column AS f_$FormatSha512Column,
                        f.$FormatCreationDateColumn AS f_$FormatCreationDateColumn
                    FROM $ImageTable i
                    JOIN $VersionTable v ON (v.$VersionImageIdColumn = i.$ImageIdColumn)
                    JOIN $FormatTable f ON (f.$FormatVersionIdColumn = v.$VersionIdColumn)
                    WHERE i.$ImageGroupColumn = $1 
                        AND i.$ImageNameColumn = $2
                        AND v.$VersionNameColumn = $3
                        AND f.$FormatTypeColumn = $4::format_type
                    """.trimIndent()
                )
                .bind("$1", group)
                .bind("$2", name)
                .bind("$3", version)
                .bind("$4", formatType.toString())
                .mapRow { row, _ -> row.toImageFormat("f", "v", "i") }
                .singleOrEmpty()
        }
        .awaitFirstOrNull()

    private fun Row.toImage(alias: String = ""): Image = Image(
        id = get(alias.columnName(ImageIdColumn), UUID::class.java)!!,
        ownerId = get(alias.columnName(ImageOwnerIdColumn), UUID::class.java)!!,
        group = get(alias.columnName(ImageGroupColumn), String::class.java)!!,
        name = get(alias.columnName(ImageNameColumn), String::class.java)!!,
        creationDate = get(alias.columnName(ImageCreationDateColumn), OffsetDateTime::class.java)!!
    )

    private fun Row.toImageFormat(alias: String, versionAlias: String, imageAlias: String): ImageFormat = ImageFormat(
        id = get(alias.columnName(FormatIdColumn), UUID::class.java)!!,
        version = toImageVersion(versionAlias, imageAlias),
        type = ImageFormat.Type.valueOf(get(alias.columnName(FormatTypeColumn), String::class.java)!!),
        uri = URI(get(alias.columnName(FormatUriColumn), String::class.java)!!),
        sha512 = get(alias.columnName(FormatSha512Column), String::class.java)!!,
        creationDate = get(alias.columnName(FormatCreationDateColumn), OffsetDateTime::class.java)!!
    )

    private fun Row.toImageVersion(alias: String, imageAlias: String): ImageVersion = ImageVersion(
        id = get(alias.columnName(VersionIdColumn), UUID::class.java)!!,
        image = toImage(imageAlias),
        name = get(alias.columnName(VersionNameColumn), String::class.java)!!
    )

    private fun String.columnName(columnName: String = ""): String = "${this}_$columnName"
}
