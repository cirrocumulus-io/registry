import java.io.ByteArrayOutputStream

plugins {
    application
    kotlin("jvm")
    id("org.liquibase.gradle") version "2.0.1"
}

application {
    applicationName = rootProject.name
    mainClassName = "io.cirrocumulus.registry.api.AppKt"
}

distributions {
    main {
        contents {
            from("etc") {
                include("*-default.yml")
                into("etc")
            }

            from("${rootProject.projectDir}/LICENSE")
        }
    }
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
    implementation(project(":cirrocumulus-registry-dto"))

    implementation("commons-codec:commons-codec:1.13")

    val coroutinesVersion = "1.3.2"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")

    implementation("org.mindrot:jbcrypt:0.4")

    val jUnitVersion = "5.5.2"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.10")

    implementation(kotlin("stdlib"))

    val kotlintestVersion = "3.4.2"
    testImplementation("io.kotlintest:kotlintest-core:$kotlintestVersion")
    testRuntime("io.kotlintest:kotlintest-runner-junit5:$kotlintestVersion")

    val ktorVersion = "1.2.4"
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") {
        exclude("org.jetbrains.kotlinx")
    }

    val liquibaseVersion = "3.8.0"
    implementation("org.liquibase:liquibase-core:$liquibaseVersion")
    liquibaseRuntime("org.liquibase:liquibase-core:$liquibaseVersion")

    testImplementation("io.mockk:mockk:1.9.3")

    runtime("ch.qos.logback:logback-classic:1.2.3")

    val postgresqlVersion = "42.2.8"
    runtime("org.postgresql:postgresql:$postgresqlVersion")
    liquibaseRuntime("org.postgresql:postgresql:$postgresqlVersion")

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
}

tasks.withType<ProcessResources> {
    filesMatching("metadata.yml") {
        expand(
            "version" to project.version,
            "build" to currentCommitHash()
        )
    }
}

tasks.withType<Test> {
    dependsOn("update")

    doFirst {
        exec {
            executable("psql")
            args(
                listOf(
                    "-h",
                    "127.0.0.1",
                    "-U",
                    "cirrocumulus_registry",
                    "-v",
                    "ON_ERROR_STOP=1",
                    "cirrocumulus_registry"
                )
            )
            environment("PGPASSWORD", "cirrocumulus_registry")
            standardInput = File("$projectDir/src/test/resources/sql/init.sql").inputStream()
        }
    }
}

fun currentCommitHash(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        executable("git")
        args(listOf("rev-parse", "--short", "HEAD"))
        standardOutput = stdout
    }
    return stdout.toString(Charsets.UTF_8)
}
