plugins {
  id("java")
  id("com.github.johnrengelman.shadow") version "7.1.2"
  id("xyz.jpenilla.run-paper") version "1.0.6"
  id("net.kyori.indra.checkstyle") version "3.0.0"
}

group = "xyz.tehbrian"
version = "2.3.0"
description = "Easily edit signs, with support for both MiniMessage and legacy formatting."

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
  mavenCentral()
  maven("https://papermc.io/repo/repository/maven-public/")
  maven("https://repo.thbn.me/releases/")
}

dependencies {
  compileOnly("io.papermc.paper:paper-api:1.19.2-R0.1-SNAPSHOT")

  implementation("cloud.commandframework:cloud-minecraft-extras:1.7.1")
  implementation("com.google.inject:guice:5.1.0")
  implementation("dev.tehbrian:tehlib-paper:0.3.1")
  implementation("org.spongepowered:configurate-yaml:4.1.2")
  implementation("xyz.tehbrian.restrictionhelper:restrictionhelper-spigot:0.3.2")
}

tasks {
  assemble {
    dependsOn(shadowJar)
  }

  processResources {
    expand("version" to project.version, "description" to project.description)
  }

  jar {
    archiveBaseName.set("YetAnotherSignEditor")
  }

  shadowJar {
    archiveClassifier.set("")

    val libsPackage = "${project.group}.${project.name}.libs"
    relocate("cloud.commandframework", "$libsPackage.cloud")
    relocate("com.google.inject", "$libsPackage.guice")
    relocate("dev.tehbrian.tehlib", "$libsPackage.tehlib")
    relocate("org.spongepowered.configurate", "$libsPackage.configurate")
    relocate("xyz.tehbrian.restrictionhelper", "$libsPackage.restrictionhelper")
  }

  runServer {
    minecraftVersion("1.19.2")
  }
}
