plugins {
    application
    kotlin("jvm")
    id("org.liquibase.gradle") version "2.0.1"
}

application {
    mainClassName = "io.cirrocumulus.registry.api.AppKt"
}

liquibase {
    activities.register("main") {
        arguments = mapOf(
                "changeLogFile" to "src/main/resources/db/changelog.xml",
                "url" to "jdbc:postgresql://localhost:5432/cirrocumulus_registry",
                "username" to "cirrocumulus_registry",
                "password" to "cirrocumulus_registry"
        )
    }
}

dependencies {
    val jUnitVersion = "5.5.2"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")

    implementation(kotlin("stdlib"))

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")

    val ktorVersion = "1.2.4"
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") {
        exclude("org.jetbrains.kotlinx")
    }

    liquibaseRuntime("org.liquibase:liquibase-core:3.8.0")
    liquibaseRuntime("org.postgresql:postgresql:42.2.8")

    runtime("ch.qos.logback:logback-classic:1.2.3")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = application.mainClassName
        attributes["Implementation-Version"] = project.version
    }

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
