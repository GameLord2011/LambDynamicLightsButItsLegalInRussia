import lambdynamiclights.Constants
import lambdynamiclights.data.Nmt
import lambdynamiclights.task.GenerateNmtTask

plugins {
	id("lambdynamiclights")
}

val prettyName = "${Constants.PRETTY_NAME} (API)"

base.archivesName.set(Constants.NAME + "-api")

tasks.generateFmj.configure {
	this.fmj.get()
		.withNamespace(Constants.NAMESPACE + "_api")
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

val generateNmt = tasks.register("generateNmt", GenerateNmtTask::class) {
	this.nmt.set(tasks.generateFmj.flatMap {
		it.fmj.map { fmj ->
			fmj.derive(::Nmt)
				.withLoaderVersion("[2,)")
				.withDepend("minecraft", "[1.21.2,1.21.3]")
		}
	})
	outputDir.set(project.file("build/generated/generated_resources/"))
}

tasks.generateFmj.configure {
	dependsOn(generateNmt)
}

tasks.ideaSyncTask.configure {
	dependsOn(generateNmt)
}

tasks.getByName("sourcesJar") {
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
