plugins {
    `java-library`
    alias(libs.plugins.jcommon)
    alias(libs.plugins.bundler)
}

group = "net.okocraft.biomelocations"
version = "1.4"

val apiVersion = "1.21"

jcommon {
    javaVersion = JavaVersion.VERSION_25

    setupPaperRepository()

    commonDependencies {
        compileOnly(libs.configurate.yaml)
        compileOnly(libs.paper.api)

        implementation(libs.codec4j.io.gson) {
            exclude("com.google.code.gson", "gson")
        }
        implementation(libs.codec4j.io.gzip)
        implementation(libs.mcmsgdef)
    }
}

bundler {
    copyToRootBuildDirectory("BiomeLocations-${project.version}")
    replacePluginVersionForPaper(project.version, apiVersion)
}

tasks.shadowJar {
    minimize()
}
