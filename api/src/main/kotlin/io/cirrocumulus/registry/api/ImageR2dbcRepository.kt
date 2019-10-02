package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.Image
import io.cirrocumulus.registry.core.ImageFormat
import io.cirrocumulus.registry.core.ImageVersion
import io.r2dbc.client.R2dbc
import io.r2dbc.spi.Row
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
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

    override suspend fun create(image: Image): Image = dbClient
        .inTransaction { handle ->
            handle
                .createUpdate(
                    """
                    INSERT INTO $ImageTable
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

    override suspend fun createFormat(format: ImageFormat): ImageFormat = dbClient
        .inTransaction { handle ->
            handle
                .execute(
                    """
                    INSERT INTO $FormatTable
                    VALUES ($1, $2, $3::format_type, $4, $5, $6)
                    """.trimIndent(),
                    format.id,
                    format.version.id,
                    format.type.toSql(),
                    format.uri,
                    format.sha512,
                    format.creationDate
                )
                .map { format }
        }
        .awaitFirst()

    override suspend fun createVersion(version: ImageVersion): ImageVersion = dbClient
        .inTransaction { handle ->
            handle
                .execute(
                    """
                    INSERT INTO $VersionTable
                    VALUES ($1, $2, $3)
                    """.trimIndent(),
                    version.id,
                    version.image.id,
                    version.name
                )
                .map { version }
        }
        .awaitSingle()

    override suspend fun find(group: String, name: String): Image? = dbClient
        .inTransaction { handle ->
            handle
                .select(
                    """
                    SELECT *
                    FROM $ImageTable
                    WHERE "$ImageGroupColumn" = $1
                        AND $ImageNameColumn = $2
                    """.trimIndent()
                )
                .bind("$1", group)
                .bind("$2", name)
                .mapRow { row, _ -> row.toImage() }
                .singleOrEmpty()
        }
        .awaitFirstOrNull()

    override suspend fun findFormat(
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
                .bind("$4", formatType.toSql())
                .mapRow { row, _ -> row.toImageFormat("f", "v", "i") }
                .singleOrEmpty()
        }
        .awaitFirstOrNull()

    override suspend fun findVersion(group: String, name: String, version: String): ImageVersion? = dbClient
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
                        v.$VersionNameColumn AS v_$VersionNameColumn
                    FROM $ImageTable i
                    JOIN $VersionTable v ON (v.$VersionImageIdColumn = i.$ImageIdColumn)
                    WHERE i.$ImageGroupColumn = $1
                        AND i.$ImageNameColumn = $2
                        AND v.$VersionNameColumn = $3
                    """.trimIndent()
                )
                .bind("$1", group)
                .bind("$2", name)
                .bind("$3", version)
                .mapRow { row, _ -> row.toImageVersion("v", "i") }
                .singleOrEmpty()
        }
        .awaitFirstOrNull()

    private fun ImageFormat.Type.toSql() = name.toLowerCase()

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
        type = get(alias.columnName(FormatTypeColumn), String::class.java)!!.toImageFormatType(),
        uri = URI(get(alias.columnName(FormatUriColumn), String::class.java)!!),
        sha512 = get(alias.columnName(FormatSha512Column), String::class.java)!!,
        creationDate = get(alias.columnName(FormatCreationDateColumn), OffsetDateTime::class.java)!!
    )

    private fun Row.toImageVersion(alias: String, imageAlias: String): ImageVersion = ImageVersion(
        id = get(alias.columnName(VersionIdColumn), UUID::class.java)!!,
        image = toImage(imageAlias),
        name = get(alias.columnName(VersionNameColumn), String::class.java)!!
    )

    private fun String.columnName(columnName: String = ""): String =
        if (isEmpty()) columnName else "${this}_$columnName"

    private fun String.toImageFormatType() = ImageFormat.Type
        .values().firstOrNull { it.toSql() == this }
        ?: throw UnsupportedFormatException("$this is not a supported format")
}
