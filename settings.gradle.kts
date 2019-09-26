rootProject.name = "cirrocumulus-registry"

fun includeModule(name: String) {
    val projectName = "${rootProject.name}-$name"
    include(projectName)
    val project = project(":$projectName")
    project.projectDir = File(name)
    project.buildFileName = "$name.gradle.kts"
}

arrayOf("api", "core").forEach { includeModule(it) }
