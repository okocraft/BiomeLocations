plugins {
    `java-library`
    id("dev.siroshun.gradle.plugins.jcommon") version "1.5.1"
    id("dev.siroshun.gradle.plugins.bundler") version "1.5.1"
}

group = "net.okocraft.biomelocations"
version = "1.3"

val apiVersion = "1.21"

jcommon {
    javaVersion = JavaVersion.VERSION_21

    setupPaperRepository()

    commonDependencies {
        compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")

        val configAPIVersion = "5.0.0-rc.2"
        implementation("dev.siroshun.configapi:configapi-core:$configAPIVersion")
        implementation("dev.siroshun.configapi:configapi-format-binary:$configAPIVersion")
        implementation("dev.siroshun.configapi:configapi-format-yaml:$configAPIVersion") {
            exclude("org.yaml", "snakeyaml")
        }
        implementation("dev.siroshun.configapi:configapi-serialization-record:$configAPIVersion")

        implementation("dev.siroshun.mcmsgdef:mcmsgdef:1.0.0-rc.2")
    }
}

bundler {
    copyToRootBuildDirectory("BiomeLocations-${project.version}")
    replacePluginVersionForBukkit(project.version, apiVersion)
}

tasks.shadowJar {
    minimize()
    relocate("com.github.siroshun09.configapi", "net.okocraft.biomelocations.libs.configapi")
    relocate("dev.siroshun.mcmsgdef", "net.okocraft.biomelocations.libs.mcmsgdef")
}
