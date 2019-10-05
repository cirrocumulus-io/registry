package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.ImageFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.io.InputStream

class FilesystemImageFileManager(
    private val config: Configuration
) : ImageFileManager {
    companion object {
        const val ImagesDirname = "images"
        private val Logger = LoggerFactory.getLogger(FilesystemImageFileManager::class.java)
    }

    override suspend fun write(
        group: String,
        name: String,
        versionName: String,
        formatType: ImageFormat.Type,
        fileInput: InputStream
    ): File = withContext(Dispatchers.IO) {
        val dir = config.registry.storageDir
            .resolve(ImagesDirname)
            .resolve(group)
            .resolve(name)
            .resolve(versionName)
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                val message = "Unable to create $dir directory"
                Logger.error(message)
                throw IOException(message)
            }
            Logger.debug("{} directory created", dir)
        }
        val file = dir.resolve(filename(name, versionName, formatType))
        file.outputStream().buffered().use { output -> fileInput.copyTo(output) }
        Logger.debug("{} saved", file)
        file
    }

    private fun filename(name: String, versionName: String, formatType: ImageFormat.Type) =
        "$name-$versionName.${formatType.fileExtension}"
}
