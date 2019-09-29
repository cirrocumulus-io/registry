package io.cirrocumulus.registry.api

import io.cirrocumulus.registry.core.Image
import io.cirrocumulus.registry.core.ImageFormat
import io.cirrocumulus.registry.core.ImageVersion
import io.cirrocumulus.registry.core.User
import io.ktor.util.InternalAPI
import io.ktor.util.encodeBase64
import java.net.URI
import java.time.OffsetDateTime
import java.time.ZoneOffset
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
    creationDate = OffsetDateTime.of(2019, 9, 28, 23, 5, 0, 0, ZoneOffset.ofHours(2))
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
    sha512 = "8f14ceb5224148cd03648aed62803ef9b1155062d1f685b3945f22e9298e8bdfa68d3372864b6b0dcc205e3e2da7befb439dfdd3c245ce9f20726936a612664d",
    creationDate = OffsetDateTime.of(2019, 9, 28, 23, 5, 0, 0, ZoneOffset.ofHours(2))
)

@InternalAPI
fun User.basicAuthentication(): String = "Basic ${"$username:changeit".encodeBase64()}"
