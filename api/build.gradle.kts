import lambdynamiclights.Constants
import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask

plugins {
	id("lambdynamiclights")
}

val prettyName = "${Constants.PRETTY_NAME} (API)"

base.archivesName.set(Constants.NAME + "-api")

dependencies {
	include(libs.yumi.commons.core)
	include(libs.yumi.commons.collections)
	include(libs.yumi.commons.event)
}

lambdamcdev {
	namespace = Constants.NAMESPACE + "_api"

	manifests {
		val fmj = fmj {
			withNamespace(Constants.NAMESPACE + "_api")
				.withName(prettyName)
				.withDescription(Constants.API_DESCRIPTION)
				.withModMenu {
					it.withBadges("library")
						.withParent(Constants.NAMESPACE, Constants.PRETTY_NAME) { parent ->
							parent.withDescription(Constants.DESCRIPTION)
								.withIcon("assets/${Constants.NAMESPACE}/icon.png")
						}
				}
		}

		nmt {
			fmj.copyTo(this)
			withLoaderVersion("[2,)")
			withDepend("minecraft", "[" + libs.versions.minecraft.get() + ",)")
		}

		fmj {
			withDepend("yumi-commons-core", "^${libs.versions.yumi.commons.get()}")
			withDepend("yumi-commons-collections", "^${libs.versions.yumi.commons.get()}")
			withDepend("yumi-commons-event", "^${libs.versions.yumi.commons.get()}")
		}
	}

	setupJarJarCompat()
}

val generateNmt = tasks.named("generateNmt")

tasks.generateFmj.configure {
	dependsOn(generateNmt)
}

val mojmap by sourceSets.creating {}
val mojangMappings by configurations.creating {}

dependencies {
	mojangMappings(loom.officialMojangMappings())
}

java {
	registerFeature("mojmap") {
		usingSourceSet(mojmap)
		withSourcesJar()

		afterEvaluate {
			configurations["mojmapApiElements"].extendsFrom(configurations["apiElements"])
			configurations["mojmapRuntimeElements"].extendsFrom(configurations["runtimeElements"])
		}
	}
}

val remapMojmap by tasks.registering(RemapJarTask::class) {
	dependsOn(tasks.remapJar)

	inputFile.set(tasks.remapJar.flatMap { it.archiveFile })
	customMappings.from(mojangMappings)
	sourceNamespace = "intermediary"
	targetNamespace = "named"
	archiveClassifier = "mojmap"
	classpath.setFrom((loom as LoomGradleExtension).getMinecraftJars(MappingsNamespace.INTERMEDIARY))

	addNestedDependencies = false // Jars have already been included in the remapJar task
}

configurations["mojmapApiElements"].artifacts.removeIf {
	true
}
artifacts.add("mojmapApiElements", remapMojmap) {
	classifier = "mojmap"
}
configurations["mojmapRuntimeElements"].artifacts.removeIf {
	true
}
artifacts.add("mojmapRuntimeElements", remapMojmap) {
	classifier = "mojmap"
}

val remapMojmapSources by tasks.registering(RemapSourcesJarTask::class) {
	dependsOn(tasks.remapSourcesJar)

	inputFile.set(tasks.remapSourcesJar.flatMap { it.archiveFile })
	customMappings.from(mojangMappings)
	sourceNamespace = "intermediary"
	targetNamespace = "named"
	archiveClassifier = "mojmap-sources"
	classpath.setFrom((loom as LoomGradleExtension).getMinecraftJars(MappingsNamespace.INTERMEDIARY))
}

// Add the remapped sources artifact
configurations["mojmapSourcesElements"].artifacts.removeIf {
	true
}
artifacts.add("mojmapSourcesElements", remapMojmapSources) {
	classifier = "mojmap-sources"
}

// Configure the maven publication.
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])

			artifactId = "lambdynamiclights-api"

			pom {
				name.set(prettyName)
				description.set(Constants.API_DESCRIPTION)
			}
		}
	}
}
