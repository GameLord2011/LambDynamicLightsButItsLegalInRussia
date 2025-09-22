import com.modrinth.minotaur.dependencies.ModDependency
import dev.lambdaurora.mcdev.api.McVersionLookup
import dev.lambdaurora.mcdev.api.ModVersionDependency
import dev.lambdaurora.mcdev.api.manifest.Nmt
import dev.lambdaurora.mcdev.task.GenerateNeoForgeJiJDataTask
import dev.lambdaurora.mcdev.task.packaging.PackageModrinthTask
import lambdynamiclights.Constants
import lambdynamiclights.Utils
import lambdynamiclights.task.AssembleFinalJarTask
import net.darkhax.curseforgegradle.TaskPublishCurseForge
import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace
import net.fabricmc.loom.task.RemapJarTask

plugins {
	id("lambdynamiclights")
	`maven-publish`
	id("com.gradleup.shadow").version("8.3.3")
	id("com.modrinth.minotaur").version("2.+")
	id("net.darkhax.curseforgegradle").version("1.1.+")
}

base.archivesName.set(Constants.NAME)

logger.lifecycle("Preparing version ${version}...")

val fabricApiModules = listOf(
	fabricApi.module("fabric-lifecycle-events-v1", libs.versions.fabric.api.get())!!,
	fabricApi.module("fabric-resource-loader-v0", libs.versions.fabric.api.get())!!,
	fabricApi.module("fabric-resource-conditions-api-v1", libs.versions.fabric.api.get())!!
)

val neoforge: SourceSet by sourceSets.creating {
	this.compileClasspath += sourceSets.main.get().compileClasspath
	this.runtimeClasspath += sourceSets.main.get().runtimeClasspath
}

tasks.generateFmj.configure {
	val fmj = this.fmj.get()
		.withEntrypoints("yumi:client_init", "dev.lambdaurora.lambdynlights.LambDynLights::INSTANCE")
		.withEntrypoints("lambdynlights:platform_provider", "dev.lambdaurora.lambdynlights.platform.fabric.FabricPlatform")
		.withEntrypoints("modmenu", "dev.lambdaurora.lambdynlights.LambDynLightsModMenu")
		.withAccessWidener("lambdynlights.accesswidener")
		.withMixins("lambdynlights.mixins.json", "lambdynlights.lightsource.mixins.json")
		.withDepend("${Constants.NAMESPACE}_api", ">=${version}")
		.withDepend("spruceui", ">=${libs.versions.spruceui.get()}")
		.withDepend("yumi_mc_core", ">=${libs.versions.yumi.mc.foundation.get()}")
		.withRecommend("modmenu", ">=${libs.versions.modmenu.get()}")
		.withBreak("optifabric", "*")
		.withBreak("sodiumdynamiclights", "*")
		.withBreak("ryoamiclights", "*")

	fabricApiModules.forEach { module -> fmj.withDepend(module.name, ">=${module.version}") }
}

lambdamcdev.manifests {
	val fmj = this.fmj().get()

	nmt {
		fmj.copyTo(this)
		withNamespace(lambdamcdev.namespace.get() + "_runtime")
		withName(Constants.PRETTY_NAME + " (Runtime)")
		withDescription(Constants.RUNTIME_DESCRIPTION)
		withLoaderVersion("[2,)")
		withYumiEntrypoints("yumi:client_init", "dev.lambdaurora.lambdynlights.LambDynLights::INSTANCE")
		withYumiEntrypoints("lambdynlights:platform_provider", "dev.lambdaurora.lambdynlights.platform.neoforge.NeoForgePlatformProvider")
		withAccessTransformer("META-INF/accesstransformer.cfg")
		withMixins("lambdynlights.mixins.json", "lambdynlights.lightsource.mixins.json")
		withDepend(Constants.NAMESPACE + "_api", "[${version},)", Nmt.DependencySide.CLIENT)
		withDepend("minecraft", "[${libs.versions.minecraft.get()},)")
		withDepend("spruceui", "[${libs.versions.spruceui.get()},)", Nmt.DependencySide.CLIENT)
		withDepend("yumi_mc_core", "[${libs.versions.yumi.mc.foundation.get()},)", Nmt.DependencySide.CLIENT)
		withBreak("sodiumdynamiclights", "*", Nmt.DependencySide.CLIENT)
		withBreak("ryoamiclights", "*", Nmt.DependencySide.CLIENT)
	}
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
	maven {
		name = "NeoForge"
		url = uri("https://maven.neoforged.net/")
		content {
			includeGroupByRegex("net\\.neoforged.*")
			includeGroupByRegex("cpw\\.mods.*")
		}
	}
}

loom {
	accessWidenerPath = file("src/main/resources/lambdynlights.accesswidener")
}

val mojmap = lambdamcdev.setupMojmapRemapping()

afterEvaluate {
	val shims: SourceSet by sourceSets.creating {
		this.compileClasspath += configurations["minecraftNamedCompile"]
	}

	dependencies {
		"shimsCompileOnly"(libs.fabric.loader) // Due to MC classes referring to EnvType.
		"shimsCompileOnly"(libs.neoforge.loader)
		"neoforgeCompileOnly"(shims.output)
	}

	license {
		exclude(shims)
	}
}

dependencies {
	api(project(":api", configuration = "namedElements"))
	include(project(":api"))

	modImplementation(libs.fabric.loader)
	fabricApiModules.forEach { modImplementation(it) }
	modImplementation(libs.yumi.mc.foundation)
	include(libs.yumi.mc.foundation)

	implementation(libs.nightconfig.core)
	implementation(libs.nightconfig.toml)
	modImplementation(libs.spruceui)
	include(libs.spruceui)
	modImplementation(libs.pridelib)
	include(libs.pridelib)

	modCompileOnly(libs.modmenu) {
		this.isTransitive = false
	}
	modLocalRuntime(libs.modmenu) {
		this.isTransitive = false
	}

	// Mod compatibility
	modCompileOnly(libs.trinkets)
	modCompileOnly(libs.accessories)

	shadow(libs.nightconfig.core)
	shadow(libs.nightconfig.toml)

	"neoforgeCompileOnly"(libs.neoforge.loader)
	"neoforgeImplementation"(sourceSets.main.get().output)

	"mojmapCompileOnly"(libs.yumi.mc.foundation)
	"mojmapCompileOnly"(libs.spruceui)
	"mojmapCompileOnly"(libs.pridelib)

	include(project(":api", configuration = "mojmapRuntimeElements"))
	include(libs.yumi.mc.foundation) {
		capabilities {
			requireCapability("dev.yumi.mc.core:yumi-mc-foundation-mojmap")
		}
	}
	include(libs.spruceui) {
		capabilities {
			requireCapability("dev.lambdaurora:spruceui-mojmap")
		}
	}
	include(libs.pridelib) {
		capabilities {
			requireCapability("io.github.queerbric:pridelib-mojmap")
		}
	}
}

tasks.processResources {
	inputs.property("version", project.version)

	filesMatching("fabric.mod.json") {
		expand("version" to inputs.properties["version"])
	}
}

tasks.shadowJar {
	dependsOn(tasks.jar)
	configurations = listOf(project.configurations["shadow"])
	destinationDirectory.set(project.layout.buildDirectory.dir("devlibs"))
	archiveClassifier.set("dev")

	relocate("com.electronwill.nightconfig", "dev.lambdaurora.lambdynlights.shadow.nightconfig")

	from(rootProject.file("LICENSE")) {
		rename { "${it}_${Constants.NAME}" }
	}
}

tasks.remapJar {
	this.dependsOn(tasks.shadowJar)

	this.archiveClassifier = "intermediary"
	this.destinationDirectory = project.layout.buildDirectory.dir("devlibs")

	this.nestedJars.setFrom(this.nestedJars.files.stream().filter {
		!it.name.endsWith("-mojmap.jar")
	}.toList())
}

val neoforgeJar = tasks.register<Jar>("neoforgeJar") {
	this.group = "build"
	this.from(neoforge.output)
	this.archiveClassifier = "neoforge-dev"
	this.destinationDirectory = project.layout.buildDirectory.dir("devlibs/neoforge")
}

val remapNeoforgeJar = tasks.register<RemapJarTask>("remapNeoforgeJarToIntermediary") {
	this.group = "remapping"
	this.dependsOn(neoforgeJar.get())
	this.inputFile.set(neoforgeJar.get().archiveFile)
	this.classpath.from(neoforge.compileClasspath)
	this.archiveClassifier = "neoforge-intermediary"
	this.destinationDirectory = project.layout.buildDirectory.dir("devlibs/neoforge")

	addNestedDependencies = false // Jars will be included later.
}
tasks.build.get().dependsOn(remapNeoforgeJar)

//region Mojmap
val remapMojmap by tasks.registering(RemapJarTask::class) {
	this.group = "remapping"
	this.dependsOn(tasks.remapJar)

	inputFile.set(tasks.remapJar.flatMap { it.archiveFile })
	customMappings.from(mojmap.mappingsConfiguration())
	sourceNamespace = "intermediary"
	targetNamespace = "named"
	archiveClassifier = "mojmap"
	classpath.setFrom(
		(loom as LoomGradleExtension).getMinecraftJars(MappingsNamespace.INTERMEDIARY),
		mojmap.sourceSet().compileClasspath
	)

	this.archiveClassifier = "mojmap"
	this.destinationDirectory = project.layout.buildDirectory.dir("devlibs")

	addNestedDependencies = false // Jars will be included later.
}

val remapNeoforgeJarToMojmap by tasks.registering(RemapJarTask::class) {
	this.group = "remapping"
	this.dependsOn(remapNeoforgeJar)

	inputFile.set(remapNeoforgeJar.flatMap { it.archiveFile })
	customMappings.from(mojmap.mappingsConfiguration())
	sourceNamespace = "intermediary"
	targetNamespace = "named"
	classpath.setFrom((loom as LoomGradleExtension).getMinecraftJars(MappingsNamespace.INTERMEDIARY))

	this.archiveClassifier = "neoforge-mojmap"
	this.destinationDirectory = project.layout.buildDirectory.dir("devlibs/neoforge")

	this.nestedJars.setFrom(this.nestedJars.files.stream().filter {
		it.name.endsWith("-mojmap.jar")
	}.toList())
}

val generateJarJarMetadata by tasks.registering(GenerateNeoForgeJiJDataTask::class) {
	val includeConfig = project.configurations.getByName("includeInternal");
	this.from(includeConfig) {
		it.name.endsWith("-mojmap")
	}
	this.outputFile.set(
		project.layout.buildDirectory
			.asFile
			.map(File::toPath)
			.map { path -> path.resolve("generated/jarjar/metadata.json").toFile() }
			.get()
	)
}

val finalJar by tasks.registering(AssembleFinalJarTask::class) {
	this.group = "build"
	this.dependsOn(
		tasks.remapJar,
		remapMojmap,
		remapNeoforgeJarToMojmap,
		generateJarJarMetadata
	)

	this.artifactGroup.set(project.group.toString())
	this.version.set(project.version.toString())
	this.fmj.set(lambdamcdev.manifests.fmj())
	this.nmt.set(lambdamcdev.manifests.nmt())
	this.runtimeIntermediaryJar.set(tasks.remapJar.flatMap { it.archiveFile })
	this.runtimeMojmapJar.set(remapMojmap.flatMap { it.archiveFile })
	this.neoforgeJar.set(remapNeoforgeJarToMojmap.flatMap { it.archiveFile })
	this.jarJarMetadata.set(generateJarJarMetadata.flatMap { it.outputFile })
}
//endregion

val packageModrinth by tasks.registering(PackageModrinthTask::class) {
	this.group = "publishing"
	this.versionType.set(Constants.getVersionType())
	this.versionName.set("${Constants.PRETTY_NAME} ${Constants.VERSION} (${McVersionLookup.getVersionTag(Constants.mcVersion())})")
	this.gameVersions.set(listOf(Constants.mcVersion()) + Constants.COMPATIBLE_MC_VERSIONS)
	this.loaders.set(listOf("fabric", "quilt"))
	this.dependencies.set(
		listOf(
			ModVersionDependency("P7dR8mSH", ModVersionDependency.Type.REQUIRED),
			ModVersionDependency("reCfnRvJ", ModVersionDependency.Type.INCOMPATIBLE),
			ModVersionDependency("PxQSWIcD", ModVersionDependency.Type.INCOMPATIBLE)
		)
	)
	this.changelog.set(Utils.fetchChangelog(project))
	this.readme.set(Utils.parseReadme(project))
	this.files.setFrom(tasks.remapJar.get())
}

modrinth {
	projectId = project.property("modrinth_id") as String
	versionName = "${Constants.PRETTY_NAME} ${Constants.VERSION} (${McVersionLookup.getVersionTag(Constants.mcVersion())})"
	uploadFile.set(tasks.remapJar.get())
	loaders.set(listOf("fabric", "quilt"))
	gameVersions.set(listOf(Constants.mcVersion()) + Constants.COMPATIBLE_MC_VERSIONS)
	versionType.set(Constants.getVersionType().toString())
	syncBodyFrom.set(Utils.parseReadme(project))
	dependencies.set(
		listOf(
			ModDependency("P7dR8mSH", "required"),
			ModDependency("reCfnRvJ", "incompatible"),
			ModDependency("PxQSWIcD", "incompatible")
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
	mainFile.addGameVersion(McVersionLookup.getCurseForgeEquivalent(Constants.mcVersion()))
	Constants.COMPATIBLE_MC_VERSIONS.stream()
		.map { McVersionLookup.getCurseForgeEquivalent(it) }
		.forEach { mainFile.addGameVersion(it) }
	mainFile.addModLoader("Fabric", "Quilt")
	mainFile.addJavaVersion("Java 21", "Java 22")
	mainFile.addEnvironment("Client")

	mainFile.displayName = "${Constants.PRETTY_NAME} ${Constants.VERSION} (${McVersionLookup.getVersionTag(Constants.mcVersion())})"
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
