plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("xyz.jpenilla.run-paper") version "1.0.6"
}

group = "xyz.tehbrian"
version = "2.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

repositories {
    mavenCentral()

    maven("https://papermc.io/repo/repository/maven-public/") {
        name = "papermc"
    }
    maven("https://s01.oss.sonatype.org/content/groups/public/") {
        name = "sonatype-s01"
    }
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype-s01-snapshots"
    }
    maven("https://repo.thbn.me/snapshots/") {
        name = "thbn-snapshots"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")

    implementation("com.google.inject:guice:5.0.1")

    implementation("net.kyori:adventure-text-minimessage:4.2.0-SNAPSHOT")

    implementation("org.spongepowered:configurate-yaml:4.1.2")

    implementation("cloud.commandframework:cloud-paper:1.6.0")
    implementation("cloud.commandframework:cloud-minecraft-extras:1.5.0")

    implementation("xyz.tehbrian.restrictionhelper:restrictionhelper-spigot:0.2.0-SNAPSHOT")
    implementation("dev.tehbrian:tehlib-paper:0.1.0-SNAPSHOT")
}

tasks {
    processResources {
        expand("version" to project.version)
    }

    shadowJar {
        archiveBaseName.set("YetAnotherSignEditor")
        archiveClassifier.set("")

        val libsPackage = "xyz.tehbrian.yetanothersigneditor.libs"
        relocate("com.google.inject", "$libsPackage.guice")
        relocate("net.kyori.adventure.text.minimessage", "$libsPackage.minimessage")
        relocate("org.spongepowered.configurate.yaml", "$libsPackage.configurate.yaml")
        relocate("xyz.tehbrian.restrictionhelper", "$libsPackage.restrictionhelper")
        relocate("dev.tehbrian.tehlib", "$libsPackage.tehlib")
        relocate("cloud.commandframework", "$libsPackage.cloud")
    }

    runServer {
        minecraftVersion("1.17.1")
    }
}
