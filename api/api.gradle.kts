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

repositories {
    // FIXME: Remove at R2DBC release
    maven("https://repo.spring.io/milestone")
}

dependencies {
    implementation(project(":cirrocumulus-registry-core"))

    val coroutinesVersion = "1.3.2"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")

    implementation("org.mindrot:jbcrypt:0.4")

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

    val r2dbcVersion = "0.8.0.RC1"
    implementation("io.r2dbc:r2dbc-client:$r2dbcVersion")
    implementation("io.r2dbc:r2dbc-pool:$r2dbcVersion")
    runtime("io.r2dbc:r2dbc-postgresql:$r2dbcVersion")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = application.mainClassName
        attributes["Implementation-Version"] = project.version
    }

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

tasks.withType<Test> {
    dependsOn("update")
}
