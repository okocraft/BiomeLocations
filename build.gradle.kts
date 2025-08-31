plugins {
    `java-library`
    alias(libs.plugins.jcommon)
    alias(libs.plugins.bundler)
}

group = "net.okocraft.biomelocations"
version = "1.3"

val apiVersion = "1.21"

jcommon {
    javaVersion = JavaVersion.VERSION_21

    setupPaperRepository()

    commonDependencies {
        compileOnly(libs.paper.api)

        implementation(libs.configapi.core)
        implementation(libs.configapi.format.binary)
        implementation(libs.configapi.format.yaml) {
            exclude("org.yaml", "snakeyaml")
        }
        implementation(libs.configapi.serialization.record)

        implementation(libs.mcmsgdef) {
            exclude("net.kyori", "adventure-api")
            exclude("net.kyori", "adventure-text-serializer-minimessage")
            exclude("org.jetbrains", "annotations")
        }
    }
}

bundler {
    copyToRootBuildDirectory("BiomeLocations-${project.version}")
    replacePluginVersionForPaper(project.version, apiVersion)
}

tasks.shadowJar {
    minimize()
}
