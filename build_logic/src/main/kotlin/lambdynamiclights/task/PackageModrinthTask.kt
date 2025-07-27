package lambdynamiclights.task

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.lambdaurora.mcdev.api.McVersionLookup
import lambdynamiclights.Constants
import lambdynamiclights.Utils
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.FileSystems
import java.nio.file.Files
import javax.inject.Inject

abstract class PackageModrinthTask @Inject constructor() : DefaultTask() {
	@get:Input
	public abstract val version: Property<String>

	@get:Input
	public abstract val changelog: Property<String>

	@get:InputFiles
	public abstract val files: ConfigurableFileCollection

	@get:Input
	public abstract val readme: Property<String>

	@get:OutputFile
	public abstract val zipOut: RegularFileProperty

	init {
		this.version.convention(this.project.version.toString())
		this.changelog.convention(Utils.fetchChangelog(this.project))
		this.readme.convention(Utils.parseReadme(this.project))
		this.zipOut.convention(this.project.layout.buildDirectory.map { it -> it.file("modrinth.zip") })
	}

	@TaskAction
	fun run() {
		val zipPath = this.zipOut.get().asFile.toPath()
		Files.deleteIfExists(zipPath)

		val jarPaths = this.files.files.stream().map { it.toPath() }.toList()

		val json = JsonObject()
		json.addProperty("version", this.version.get())
		json.addProperty("name", "${Constants.PRETTY_NAME} ${Constants.VERSION} (${McVersionLookup.getVersionTag(Constants.mcVersion())})")
		json.addProperty("type", Constants.getVersionType())
		json.addProperty("changelog", this.changelog.get())

		val gameVersions = JsonArray()
		(listOf(Constants.mcVersion()) + Constants.COMPATIBLE_MC_VERSIONS).forEach { gameVersions.add(it) }
		json.add("game_versions", gameVersions)

		val loaders = JsonArray()
		loaders.add("fabric")
		loaders.add("quilt")
		json.add("loaders", loaders)

		val files = JsonArray()
		jarPaths.forEach { jarPath ->
			files.add(jarPath.fileName.toString())
		}
		json.add("files", files)

		FileSystems.newFileSystem(
			this.zipOut.get().asFile.toPath(),
			mapOf(Pair("create", "true"))
		).use { fs ->
			Files.writeString(fs.getPath("manifest.json"), GSON.toJson(json))
			Files.writeString(fs.getPath("README.md"), this.readme.get())

			jarPaths.forEach { jarPath ->
				Files.copy(jarPath, fs.getPath(jarPath.fileName.toString()))
			}
		}
	}

	companion object {
		private val GSON = GsonBuilder().setPrettyPrinting().create()
	}
}