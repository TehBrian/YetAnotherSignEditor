plugins {
	id("java")
	id("com.gradleup.shadow") version "9.4.2"
	id("xyz.jpenilla.run-paper") version "3.0.2"
	id("net.kyori.indra.checkstyle") version "4.0.0"
	id("com.github.ben-manes.versions") version "0.54.0"
}

group = "dev.tehbrian"
version = "3.0.0-SNAPSHOT"
description = "Easily edit and format signs with both MiniMessage and legacy formatting."

java {
	toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
	mavenCentral()
	maven("https://repo.papermc.io/repository/maven-public/")
	maven("https://repo.tehbrian.dev/releases/")
}

dependencies {
	compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
	compileOnly("org.jspecify:jspecify:1.0.0")
	implementation("org.bstats:bstats-bukkit:3.2.1")
	implementation("org.incendo:cloud-paper:2.0.0-beta.15")
	implementation("org.incendo:cloud-minecraft-extras:2.0.0-beta.15")
	implementation("com.google.inject:guice:7.0.0")
	implementation("org.spongepowered:configurate-yaml:4.2.0")
	implementation("dev.tehbrian:agna-paper:1.0.1")
	implementation("dev.tehbrian:mayi-paper:1.0.0")
}

tasks {
	assemble {
		dependsOn(shadowJar)
	}

	processResources {
		expand(
				mapOf(
						"version" to project.version,
						"description" to project.description
				)
		)
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
				"com.google.common",
				"com.google.errorprone",
				"com.google.inject",
				"com.google.j2objc",
				"com.google.thirdparty",
				"dev.tehbrian.agna",
				"dev.tehbrian.mayi",
				"io.leangen",
				"jakarta.inject",
				"javax.annotation",
				"net.kyori.option",
				"love.broccolai.corn",
				"org.aopalliance",
				"org.bstats",
				"org.checkerframework",
				"org.incendo.cloud",
				"org.spongepowered",
		)
	}

	runServer {
		minecraftVersion("26.1.2")
	}
}
