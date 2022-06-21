plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("net.kyori.indra.checkstyle") version "2.1.1"
}

group = "xyz.tehbrian"
version = "2.2.0"
description = "Easily edit signs, with support for both MiniMessage and legacy formatting."

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/") {
        name = "papermc"
    }
    maven("https://repo.thbn.me/releases/") {
        name = "thbn"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")

    implementation("com.google.inject:guice:5.1.0")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    implementation("cloud.commandframework:cloud-minecraft-extras:1.7.0")

    implementation("xyz.tehbrian.restrictionhelper:restrictionhelper-spigot:0.3.0")
    implementation("dev.tehbrian:tehlib-paper:0.3.0")
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    processResources {
        expand("version" to project.version, "description" to project.description)
    }

    shadowJar {
        archiveBaseName.set("YetAnotherSignEditor")
        archiveClassifier.set("")

        val libsPackage = "xyz.tehbrian.yetanothersigneditor.libs"
        relocate("com.google.inject", "$libsPackage.guice")
        relocate("org.spongepowered.configurate", "$libsPackage.configurate")
        relocate("xyz.tehbrian.restrictionhelper", "$libsPackage.restrictionhelper")
        relocate("dev.tehbrian.tehlib", "$libsPackage.tehlib")
        relocate("cloud.commandframework", "$libsPackage.cloud")
    }

    runServer {
        minecraftVersion("1.18.2")
    }
}
