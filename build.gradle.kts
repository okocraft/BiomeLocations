plugins {
    `java-library`
    id("com.gradleup.shadow") version "8.3.9"
}

group = "net.okocraft.biomelocations"
version = "1.3"

val apiVersion = "1.20"
val javaVersion = JavaVersion.VERSION_21

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

repositories {
    mavenCentral()

    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

val configAPIVersion = "5.0.0-rc.2"

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
    implementation("dev.siroshun.configapi:configapi-core:$configAPIVersion")
    implementation("dev.siroshun.configapi:configapi-format-binary:$configAPIVersion")
    implementation("dev.siroshun.configapi:configapi-format-yaml:$configAPIVersion") {
        exclude("org.yaml", "snakeyaml")
    }
    implementation("dev.siroshun.configapi:configapi-serialization-record:$configAPIVersion")

    implementation("com.github.siroshun09.messages:messages-minimessage:0.8.0")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(javaVersion.ordinal + 1)
    }

    processResources {
        filesMatching(listOf("plugin.yml")) {
            expand(
                    "apiVersion" to apiVersion,
                    "projectVersion" to version,
            )
        }
    }

    jar {
        archiveFileName = "BiomeLocations-$version-original.jar"
        manifest {
            attributes("paperweight-mappings-namespace" to "mojang")
        }
    }

    shadowJar {
        minimize()
        relocate("com.github.siroshun09.configapi", "net.okocraft.biomelocations.libs.configapi")
        relocate("com.github.siroshun09.messages", "net.okocraft.biomelocations.libs.messages")
        archiveFileName = "BiomeLocations-$version.jar"
    }
}
