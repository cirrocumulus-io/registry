plugins {
    kotlin("jvm")
}

dependencies {
    val jUnitVersion = "5.5.2"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")

    val jacksonVersion = "2.9.10"
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    testRuntime("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    implementation(kotlin("stdlib"))

    val kotlintestVersion = "3.4.2"
    testImplementation("io.kotlintest:kotlintest-core:$kotlintestVersion")
    testRuntime("io.kotlintest:kotlintest-runner-junit5:$kotlintestVersion")
}
