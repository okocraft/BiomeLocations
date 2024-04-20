plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "net.okocraft.biomelocations"
version = "1.1"

val apiVersion = "1.20"
val mcVersion = "$apiVersion.4"
val javaVersion = JavaVersion.VERSION_17

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

dependencies {
    compileOnly("io.papermc.paper:paper-api:$mcVersion-R0.1-SNAPSHOT")
    implementation("com.github.siroshun09.configapi:configapi-format-yaml:5.0.0-beta.3") {
        exclude("org.yaml", "snakeyaml")
    }
    implementation("com.github.siroshun09.messages:messages-minimessage:0.7.0")
    // You can get the jar from https://github.com/Siroshun09/BiomeFinder/releases
    implementation(files(file("libs").resolve("BiomeFinder-1.9-mc$mcVersion.jar")))
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

    shadowJar {
        minimize()
        relocate("com.github.siroshun09.configapi", "net.okocraft.biomelocations.libs.configapi")
        relocate("com.github.siroshun09.biomefinder", "net.okocraft.biomelocations.libs.biomefinder") {
            include("com.github.siroshun09.biomefinder.util.MapWalker")
            include("com.github.siroshun09.biomefinder.wrapper.**")
        }
        archiveFileName = "BiomeLocations-$version-mc$mcVersion.jar"
    }
}
