plugins {
    application
    kotlin("jvm")
}

application {
    mainClassName = "io.cirrocumulus.registry.api.AppKt"
}

dependencies {
    implementation(kotlin("stdlib"))

    val ktorVersion = "1.2.4"
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")

    runtime("org.slf4j:slf4j-simple:1.7.28")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = application.mainClassName
        attributes["Implementation-Version"] = project.version
    }

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
