import com.modrinth.minotaur.dependencies.ModDependency
import lambdynamiclights.Constants
import lambdynamiclights.Utils
import net.darkhax.curseforgegradle.TaskPublishCurseForge

plugins {
	id("lambdynamiclights")
	`maven-publish`
	id("com.gradleup.shadow").version("8.3.3")
	id("com.modrinth.minotaur").version("2.+")
	id("net.darkhax.curseforgegradle").version("1.1.+")
}

base.archivesName.set(Constants.NAME)

if (!(System.getenv("CURSEFORGE_TOKEN") != null || System.getenv("MODRINTH_TOKEN") != null || System.getenv("LDL_MAVEN") != null)) {
	version = (version as String) + "-local"
}
logger.lifecycle("Preparing version ${version}...")

val fabricApiModules = listOf(
	fabricApi.module("fabric-lifecycle-events-v1", libs.versions.fabric.api.get())!!,
	fabricApi.module("fabric-resource-loader-v0", libs.versions.fabric.api.get())!!,
	fabricApi.module("fabric-rendering-v1", libs.versions.fabric.api.get())!!
)

tasks.generateFmj.configure {
	val fmj = this.fmj.get()
		.withEntrypoints("client", "dev.lambdaurora.lambdynlights.LambDynLights")
		.withEntrypoints("modmenu", "dev.lambdaurora.lambdynlights.LambDynLightsModMenu")
		.withAccessWidener("lambdynlights.accesswidener")
		.withMixins("lambdynlights.mixins.json", "lambdynlights.lightsource.mixins.json")
		.withDepend("${Constants.NAMESPACE}_api", ">=${version}")
		.withDepend("spruceui", ">=${libs.versions.spruceui.get()}")
		.withRecommend("modmenu", ">=${libs.versions.modmenu.get()}")
		.withBreak("optifabric", "*")
		.withBreak("sodiumdynamiclights", "*")
		.withBreak("ryoamiclights", "*")

	fabricApiModules.forEach { module -> fmj.withDepend(module.name, ">=${module.version}") }
}

repositories {
	mavenLocal()
	maven {
		name = "Terraformers"
		url = uri("https://maven.terraformersmc.com/releases/")
	}
	maven {
		name = "ParchmentMC"
		url = uri("https://maven.parchmentmc.org")
	}
	maven {
		name = "Ladysnake Libs"
		url = uri("https://maven.ladysnake.org/releases")
	}
	maven { url = uri("https://maven.wispforest.io/releases") }
}

loom {
	accessWidenerPath = file("src/main/resources/lambdynlights.accesswidener")
}

dependencies {
	api(project(":api", configuration = "namedElements"))
	include(project(":api"))

	modImplementation(libs.fabric.loader)
	fabricApiModules.forEach { modImplementation(it) }
	//modRuntimeOnly(fabricApi.module("fabric-renderer-indigo", libs.versions.fabric.api.get()))

	implementation(libs.nightconfig.core)
	implementation(libs.nightconfig.toml)
	modImplementation(libs.spruceui)
	include(libs.spruceui)
	modImplementation(libs.pridelib)
	include(libs.pridelib)

	modImplementation(libs.modmenu) {
		this.isTransitive = false
	}

	// Mod compatibility
	modCompileOnly(libs.trinkets)
	modCompileOnly(libs.accessories)

	shadow(libs.yumi.commons.core) {
		isTransitive = false
	}
	shadow(libs.yumi.commons.collections) {
		isTransitive = false
	}
	shadow(libs.yumi.commons.event) {
		isTransitive = false
	}
	shadow(libs.nightconfig.core)
	shadow(libs.nightconfig.toml)
}

tasks.processResources {
	val version = project.version
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.shadowJar {
	dependsOn(tasks.jar)
	configurations = listOf(project.configurations["shadow"])
	destinationDirectory.set(file("${project.layout.buildDirectory.get()}/devlibs"))
	archiveClassifier.set("dev")

	relocate("com.electronwill.nightconfig", "dev.lambdaurora.lambdynlights.shadow.nightconfig")
}

tasks.remapJar {
	dependsOn(tasks.shadowJar)
}

modrinth {
	projectId = project.property("modrinth_id") as String
	versionName = "${Constants.PRETTY_NAME} ${Constants.VERSION} (${Constants.mcVersion()})"
	uploadFile.set(tasks.remapJar.get())
	loaders.set(listOf("fabric", "quilt"))
	gameVersions.set(listOf(Constants.mcVersion()))
	versionType.set(Constants.getVersionType())
	syncBodyFrom.set(Utils.parseReadme(project))
	dependencies.set(
		listOf(
			ModDependency("P7dR8mSH", "required")
		)
	)

	// Changelog fetching
	val changelogContent = Utils.fetchChangelog(project)

	if (changelogContent != null) {
		changelog.set(changelogContent)
	} else {
		afterEvaluate {
			tasks.modrinth.get().isEnabled = false
		}
	}
}

tasks.register<TaskPublishCurseForge>("curseforge") {
	this.group = "publishing"

	val token = System.getenv("CURSEFORGE_TOKEN")
	if (token != null) {
		this.apiToken = token
	} else {
		this.isEnabled = false
		return@register
	}

	// Changelog fetching
	var changelogContent = Utils.fetchChangelog(project)

	if (changelogContent != null) {
		changelogContent = "Changelog:\n\n${changelogContent}"
	} else {
		this.isEnabled = false
		return@register
	}

	val mainFile = upload(project.property("curseforge_id"), tasks.remapJar.get())
	mainFile.releaseType = Constants.getVersionType()
	mainFile.addGameVersion(Constants.mcVersion())
	mainFile.addModLoader("Fabric", "Quilt")
	mainFile.addJavaVersion("Java 21", "Java 22")
	mainFile.addEnvironment("Client")

	mainFile.displayName = "${Constants.PRETTY_NAME} ${Constants.VERSION} (${Constants.mcVersion()})"
	mainFile.addRequirement("fabric-api")
	mainFile.addOptional("modmenu")
	mainFile.addIncompatibility("optifabric")

	mainFile.changelogType = "markdown"
	mainFile.changelog = changelogContent
}

// Configure the maven publication.
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])

			artifactId = "lambdynamiclights-runtime"

			pom {
				name.set(Constants.PRETTY_NAME)
				description.set(Constants.DESCRIPTION)
			}
		}
	}
}
