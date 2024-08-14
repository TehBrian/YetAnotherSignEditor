plugins {
	id("java")
	id("com.github.johnrengelman.shadow") version "8.1.1"
	id("xyz.jpenilla.run-paper") version "2.3.0"
	id("net.kyori.indra.checkstyle") version "3.1.3"
	id("com.github.ben-manes.versions") version "0.51.0"
}

group = "dev.tehbrian"
version = "3.0.0-SNAPSHOT"
description = "Easily edit and format signs with both MiniMessage and legacy formatting."

java {
	toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
	mavenCentral()
	maven("https://papermc.io/repo/repository/maven-public/")
	maven("https://repo.thbn.me/releases/")
}

dependencies {
	compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")

	compileOnly("org.jspecify:jspecify:1.0.0")
	implementation("cloud.commandframework:cloud-paper:1.8.4")
	implementation("cloud.commandframework:cloud-minecraft-extras:1.8.4") {
		exclude("net.kyori", "adventure-api")
		exclude("net.kyori", "adventure-text-serializer-plain")
	}
	implementation("com.google.inject:guice:7.0.0")
	implementation("org.spongepowered:configurate-yaml:4.1.2")
	implementation("dev.tehbrian:tehlib-paper:0.6.0")
	implementation("dev.tehbrian:restrictionhelper-spigot:0.5.0")
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
		fun moveToLibs(vararg patterns: String) {
			for (pattern in patterns) {
				relocate(pattern, "$libsPackage.$pattern")
			}
		}

		moveToLibs(
			"love.broccolai.corn",
			"cloud.commandframework",
			"com.google",
			"dev.tehbrian.restrictionhelper",
			"dev.tehbrian.tehlib",
			"io.leangen",
			"jakarta.inject",
			"javax.annotation",
			"net.kyori.examination",
			"org.aopalliance",
			"org.checkerframework",
			"org.spongepowered",
			"org.yaml",
		)
	}

	runServer {
		minecraftVersion("1.21.1")
	}
}
