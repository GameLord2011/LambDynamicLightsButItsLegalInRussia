import lambdynamiclights.Constants

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
			withDepend("minecraft", "[1.21,1.21.1]")
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
