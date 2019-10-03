package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.ImageFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream

class FilesystemImageFileManager(
    private val config: Configuration
) : ImageFileManager {
    override suspend fun write(
        group: String,
        name: String,
        version: String,
        formatType: ImageFormat.Type,
        imageFileInput: InputStream
    ): File = withContext(Dispatchers.IO) {
        val dir = config.registry.imagesDir
            .resolve(group)
            .resolve(name)
            .resolve(version)
        if (!dir.exists() && !dir.mkdirs()) {
            throw IOException("Unable to create ${dir.absolutePath} directory")
        }
        val file = dir.resolve(filename(name, version, formatType))
        file.outputStream().buffered().use { output -> imageFileInput.copyTo(output) }
        file
    }

    private fun filename(name: String, version: String, formatType: ImageFormat.Type) =
        "$name-$version.${formatType.fileExtension}"
}
