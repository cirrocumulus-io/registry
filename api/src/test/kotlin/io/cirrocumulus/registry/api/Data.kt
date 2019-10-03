package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.Image
import io.cirrocumulus.registry.core.ImageFormat
import io.cirrocumulus.registry.core.ImageVersion
import io.cirrocumulus.registry.core.User
import io.ktor.util.InternalAPI
import io.ktor.util.encodeBase64
import java.io.File
import java.net.URI
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

val Config = Configuration()
val DbClient = Config.db.createClient()

const val ClearPassword = "changeit"
val User1 = User(
    id = UUID.fromString("3c2149ac-e838-4715-9cb0-1b3427810c49"),
    username = "user1",
    password = "\$2a\$12\$21SFFJSkRWjAeFt21v5mOe6lzDb7bvDfgcBVG66UB6/2mBYv8xOxS"
)
val Image1_1 = Image(
    id = UUID.fromString("8fe183ec-b45c-4877-8679-7b5fc3b9935e"),
    ownerId = User1.id,
    group = User1.username,
    name = "debian",
    creationDate = ZonedDateTime.of(2019, 9, 28, 23, 5, 0, 0, ZoneOffset.ofHours(2))
)

val ImageVersion1_1 = ImageVersion(
    id = UUID.fromString("1453b827-49bc-4ecc-bff4-d2b70c2caff3"),
    image = Image1_1,
    name = "9.0"
)
val ImageFormat1_1 = ImageFormat(
    id = UUID.fromString("93f5d4e7-4181-4c5e-afb3-66de2395869d"),
    version = ImageVersion1_1,
    type = ImageFormat.Type.Qcow2,
    uri = URI("/v1/user1/debian/9.0/qcow2"),
    sha512 = "b5ec5e13aebc7eee4b0b6f2352225a99f23dbdd4317c2cb79e786d3ebb4a1b4984fdc444ee95862f976e645f0667e64380acc4f1a77d47502097d572a42f592a",
    creationDate = ZonedDateTime.of(2019, 9, 28, 23, 5, 0, 0, ZoneOffset.ofHours(2))
)

val Qcow2ImageFile = File(DefaultImageHandlerTest::class.java.getResource("/test.qcow2").file)

@InternalAPI
fun User.basicAuthentication(): String = "Basic ${"$username:changeit".encodeBase64()}"
