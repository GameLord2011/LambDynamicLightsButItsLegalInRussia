import lambdynamiclights.Constants
import lambdynamiclights.data.Fmj
import lambdynamiclights.mappings.MojangMappingsSpec
import lambdynamiclights.task.GenerateFmjTask
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
	id("lambdynamiclights-common")
	`java-library`
	`maven-publish`
	id("dev.yumi.gradle.licenser")
}

// Seriously you should not worry about it, definitely not a hack.
// https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
val libs = the<LibrariesForLibs>()
Constants.finalizeInit(libs)

val generateFmj = tasks.register("generateFmj", GenerateFmjTask::class) {
	this.fmj.set(
		Fmj(Constants.NAMESPACE, Constants.PRETTY_NAME, project.version.toString())
			.withDescription(Constants.DESCRIPTION)
			.withAuthors(Constants.AUTHORS)
			.withContributors(Constants.CONTRIBUTORS)
			.withContact {
				it.withHomepage(Constants.PROJECT_LINK)
					.withSources(Constants.SOURCES_LINK)
					.withIssues("${Constants.SOURCES_LINK}/issues")
			}
			.withLicense(Constants.LICENSE)
			.withIcon("assets/${Constants.NAMESPACE}/icon.png")
			.withEnvironment("client")
			.withDepend("fabricloader", ">=${libs.versions.fabric.loader.get()}")
			.withDepend("minecraft", "~1.21.2- <1.21.4-")
			.withDepend("java", ">=${Constants.JAVA_VERSION}")
			.withModMenu {
				it.withLink("modmenu.curseforge", "https://www.curseforge.com/minecraft/mc-mods/lambdynamiclights")
					.withLink("modmenu.discord", "https://discord.lambdaurora.dev/")
					.withLink("modmenu.github_releases", "${Constants.SOURCES_LINK}/releases")
					.withLink("modmenu.modrinth", "https://modrinth.com/mod/lambdynamiclights")
					.withLink("modmenu.bluesky", "https://bsky.app/profile/lambdaurora.dev")
			}
	)
	outputDir.set(project.file("build/generated/generated_resources/"))
}

tasks.ideaSyncTask.configure {
	dependsOn(generateFmj)
}

sourceSets {
	main {
		resources {
			// This is needed so that people can use their IDE to compile the project (bypassing Gradle).
			srcDir(generateFmj)
		}
	}
}

dependencies {
	@Suppress("UnstableApiUsage")
	mappings(loom.layered {
		addLayer(MojangMappingsSpec(false))
		// Parchment is currently broken when used with the hacked mojmap layer due to remapping shenanigans.
		//parchment("org.parchmentmc.data:parchment-${Constants.getMcVersionString()}:${libs.versions.mappings.parchment.get()}@zip")
		mappings("dev.lambdaurora:yalmm:${Constants.mcVersion()}+build.${libs.versions.mappings.yalmm.get()}")
	})

	api(libs.yumi.commons.event) {
		// Exclude Minecraft and loader-provided libraries.
		exclude(group = "org.slf4j")
		exclude(group = "org.ow2.asm")
	}
}

tasks.jar {
	from(rootProject.file("LICENSE")) {
		rename { "${it}_${Constants.NAME}" }
	}
}

tasks.getByName("sourcesJar") {
	dependsOn(generateFmj)
}

license {
	rule(rootProject.file("metadata/HEADER"))
}

publishing {
	repositories {
		mavenLocal()
		maven {
			name = "BuildDirLocal"
			url = uri("${rootProject.layout.buildDirectory.get()}/repo")
		}

		val ldlMaven = System.getenv("LDL_MAVEN")
		if (ldlMaven != null) {
			maven {
				name = "LambDynamicLightsMaven"
				url = uri(ldlMaven)
				credentials {
					username = (project.findProperty("gpr.user") as? String) ?: System.getenv("MAVEN_USERNAME")
					password = (project.findProperty("gpr.key") as? String) ?: System.getenv("MAVEN_PASSWORD")
				}
			}
		}
	}
}
