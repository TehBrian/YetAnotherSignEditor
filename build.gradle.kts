plugins {
  id("java")
  id("com.github.johnrengelman.shadow") version "8.1.1"
  id("xyz.jpenilla.run-paper") version "2.3.0"
  id("net.kyori.indra.checkstyle") version "3.1.3"
  id("com.github.ben-manes.versions") version "0.51.0"
}

group = "dev.tehbrian"
version = "2.3.1"
description = "Easily edit and format signs with both MiniMessage and legacy formatting."

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
  mavenCentral()
  maven("https://papermc.io/repo/repository/maven-public/")
  maven("https://repo.thbn.me/releases/")
}

dependencies {
  compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

  implementation("cloud.commandframework:cloud-paper:1.8.4")
  implementation("cloud.commandframework:cloud-minecraft-extras:1.8.4")
  implementation("com.google.inject:guice:7.0.0")
  implementation("dev.tehbrian:tehlib-paper:0.5.0")
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

  base {
    archivesName.set("YetAnotherSignEditor")
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
    minecraftVersion("1.20.4")
  }
}
