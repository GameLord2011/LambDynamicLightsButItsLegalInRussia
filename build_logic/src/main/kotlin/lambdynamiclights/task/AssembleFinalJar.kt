package lambdynamiclights.task

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.lambdaurora.mcdev.api.AccessWidenerToTransformer
import dev.lambdaurora.mcdev.api.manifest.Fmj
import dev.lambdaurora.mcdev.api.manifest.Nmt
import dev.lambdaurora.mcdev.util.JsonUtils
import lambdynamiclights.Constants
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import javax.inject.Inject

abstract class AssembleFinalJar @Inject constructor() : Jar() {
	@get:Input
	abstract val artifactGroup: Property<String>

	@get:Input
	abstract val version: Property<String>

	@get:Input
	abstract val fmj: Property<Fmj>

	@get:Input
	abstract val nmt: Property<Nmt>

	@get:InputFile
	abstract val runtimeIntermediaryJar: RegularFileProperty

	@get:InputFile
	abstract val runtimeMojmapJar: RegularFileProperty

	@get:InputFile
	abstract val neoforgeJar: RegularFileProperty

	@get:InputFile
	abstract val jarJarMetadata: RegularFileProperty

	@TaskAction
	fun assemble() {
		val outputJar = this.archiveFile.get().asFile.toPath()
		val runtimeIntermediaryJarPath = this.runtimeIntermediaryJar.get().asFile.toPath()
		val neoforgeJarPath = this.neoforgeJar.get().asFile.toPath()

		FileSystems.newFileSystem(outputJar).use { outFs ->
			val outJarsDir = outFs.getPath("META-INF/jars")
			val outFabricJar = outJarsDir.resolve(
				runtimeIntermediaryJarPath.fileName.toString().replace("-intermediary", "-fabric")
			)
			val outNeoForgeJar = outJarsDir.resolve(
				neoforgeJarPath.fileName.toString().replace("-mojmap", "")
			)

			Files.createDirectories(outJarsDir)
			Files.copy(
				runtimeIntermediaryJarPath,
				outFabricJar
			)
			Files.copy(
				neoforgeJarPath,
				outNeoForgeJar
			)

			FileSystems.newFileSystem(outFabricJar).use { fabricJarFs ->
				FileSystems.newFileSystem(outNeoForgeJar).use { neoJarFs ->
					this.doAssemble(outFs, fabricJarFs, neoJarFs)
					this.writeFmj(fabricJarFs, outFs, outFabricJar)
					this.writeNmt(outFs)
					this.handleJarJar(neoJarFs, outFs, outNeoForgeJar)
					this.cleanupNeoJar(neoJarFs)
				}
			}
		}
	}

	private fun doAssemble(outFs: FileSystem, fabricJarFs: FileSystem, neoJarFs: FileSystem) {
		Files.createDirectories(outFs.getPath("dev/lambdaurora/lambdynlights"))
		this.move("dev/lambdaurora/lambdynlights/shadow", fabricJarFs, outFs)
		this.move("META-INF/versions", fabricJarFs, outFs)
		this.move("assets", fabricJarFs, outFs)
		this.move("lambdynlights.toml", fabricJarFs, outFs)
		this.move("META-INF/neoforge.mods.toml", fabricJarFs, neoJarFs)
		this.copy("LICENSE_lambdynamiclights", fabricJarFs, outFs)

		this.copy("assets/lambdynlights/icon.png", outFs, fabricJarFs)
		this.copy("assets/lambdynlights/icon.png", outFs, neoJarFs)

		this.openFs(this.runtimeMojmapJar).use { runtimeMojmapJarFs ->
			this.recursivelyCopyMojmap(runtimeMojmapJarFs.getPath("dev"), neoJarFs.getPath("dev"))
			AccessWidenerToTransformer.convert(
				runtimeMojmapJarFs.getPath("lambdynlights.accesswidener"),
				neoJarFs.getPath("META-INF/accesstransformer.cfg")
			)
			runtimeMojmapJarFs.rootDirectories.forEach {
				Files.list(it)
					.filter { path -> !path.fileName.toString().startsWith("fabric") && !path.fileName.toString().endsWith("accesswidener") }
					.filter { path -> Files.isRegularFile(path) }
					.forEach { path -> Files.copy(path, neoJarFs.getPath(path.toString())) }
			}

			this.copy("lambdynlights.mixins.json", runtimeMojmapJarFs, neoJarFs)
			this.copy("lambdynlights.lightsource.mixins.json", runtimeMojmapJarFs, neoJarFs)
		}
	}

	private fun cleanupNeoJar(fs: FileSystem) {
		Files.deleteIfExists(fs.getPath("fabric.mod.json"))
		this.recursivelyDelete(fs.getPath("dev/lambdaurora/lambdynlights/shadow"))
	}

	private fun writeFmj(fabricFs: FileSystem, outFs: FileSystem, outFabricJar: Path) {
		val fmjPath = fabricFs.getPath("fabric.mod.json")
		val json = JsonParser.parseString(Files.readString(fmjPath)).asJsonObject

		json.addProperty("id", Constants.NAMESPACE + "_runtime")
		json.addProperty("name", Constants.PRETTY_NAME + " (Runtime)")

		val modmenuObject = json.getAsJsonObject("custom").getAsJsonObject("modmenu")
		val parent = JsonObject()
		modmenuObject.add("parent", parent)
		parent.addProperty("id", Constants.NAMESPACE)
		parent.addProperty("name", Constants.PRETTY_NAME)

		Files.writeString(fmjPath, JsonUtils.GSON.toJson(json))

		val parentFmj = this.fmj.get().derive(::Fmj)
		parentFmj.withDepend(Constants.NAMESPACE + "_runtime", ">=${this.version.get()}")
		parentFmj.withEnvironment(this.fmj.get().environment)
		parentFmj.withJar(outFabricJar.toString())
		parentFmj.withModMenu(this.fmj.get().modMenu.copy())
		Files.writeString(outFs.getPath("fabric.mod.json"), JsonUtils.GSON.toJson(parentFmj))
	}

	private fun handleJarJar(neoFs: FileSystem, outFs: FileSystem, neoJarPath: Path) {
		this.move("META-INF/jars", neoFs, outFs)
		Files.createDirectories(outFs.getPath("META-INF/jarjar"))

		val jarJarMetadata = JsonParser.parseString(Files.readString(this.jarJarMetadata.get().asFile.toPath()))
			.asJsonObject

		val jars = jarJarMetadata.get("jars").asJsonArray
		val neoForgeJarEntry = JsonObject()

		val identifier = JsonObject()
		identifier.addProperty("group", this.artifactGroup.get())
		identifier.addProperty("artifact", Constants.NAMESPACE + "-runtime-neoforge")
		neoForgeJarEntry.add("identifier", identifier)

		val version = JsonObject()
		version.addProperty("range", "[${this.version.get()},)")
		version.addProperty("artifactVersion", this.version.get())
		neoForgeJarEntry.add("version", version)

		neoForgeJarEntry.addProperty("path", neoJarPath.toString())
		neoForgeJarEntry.addProperty("isObfuscated", false)

		jars.add(neoForgeJarEntry)

		Files.writeString(
			outFs.getPath("META-INF/jarjar/metadata.json"),
			JsonUtils.GSON.toJson(jarJarMetadata)
		)
	}

	private fun writeNmt(outFs: FileSystem) {
		val parentNmt = this.nmt.get().derive(::Nmt)
		parentNmt.withNamespace(Constants.NAMESPACE)
		parentNmt.withName(Constants.PRETTY_NAME)
		parentNmt.withLoaderVersion(this.nmt.get().loaderVersion)
		parentNmt.withBlurIcon(this.nmt.get().shouldBlurIcon())
		parentNmt.withDepend(
			Constants.NAMESPACE + "_runtime",
			"[${this.version.get()},)",
			Nmt.DependencySide.CLIENT
		)

		Files.writeString(outFs.getPath("META-INF/neoforge.mods.toml"), parentNmt.toToml())
	}

	private fun openFs(jar: RegularFileProperty): FileSystem {
		return FileSystems.newFileSystem(jar.get().asFile.toPath())
	}

	private fun copy(source: Path, target: Path) {
		if (target.parent != null) {
			Files.createDirectories(target.parent)
		}

		if (Files.isRegularFile(source)) {
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
			return
		}

		Files.walkFileTree(source, object : SimpleFileVisitor<Path>() {
			fun resolve(subSource: Path): Path {
				val relative = source.relativize(subSource)
				return target.resolve(relative)
			}

			override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
				Files.createDirectories(this.resolve(dir))
				return FileVisitResult.CONTINUE
			}

			override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
				Files.copy(file, this.resolve(file), StandardCopyOption.REPLACE_EXISTING)
				return FileVisitResult.CONTINUE
			}
		})
	}

	private fun copy(path: String, sourceFs: FileSystem, targetFs: FileSystem) {
		this.copy(sourceFs.getPath(path), targetFs.getPath(path))
	}

	private fun recursivelyCopyMojmap(source: Path, target: Path) {
		Files.walkFileTree(source, object : SimpleFileVisitor<Path>() {
			fun resolve(subSource: Path): Path {
				val relative = source.relativize(subSource)
				return target.resolve(relative)
			}

			override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
				if (dir.endsWith("fabric")) {
					return FileVisitResult.SKIP_SUBTREE
				}
				Files.createDirectories(this.resolve(dir))
				return FileVisitResult.CONTINUE
			}

			override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
				Files.copy(file, this.resolve(file), StandardCopyOption.REPLACE_EXISTING)
				return FileVisitResult.CONTINUE
			}
		})
	}

	private fun move(source: Path, target: Path) {
		if (target.parent != null) {
			Files.createDirectories(target.parent)
		}

		if (Files.isRegularFile(source)) {
			Files.move(source, target)
			return
		}

		Files.walkFileTree(source, object : SimpleFileVisitor<Path>() {
			fun resolve(subSource: Path): Path {
				val relative = source.relativize(subSource)
				return target.resolve(relative)
			}

			override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
				Files.createDirectories(this.resolve(dir))
				return FileVisitResult.CONTINUE
			}

			override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
				Files.move(file, this.resolve(file))
				return FileVisitResult.CONTINUE
			}
		})
		this.recursivelyDelete(source)
	}

	private fun move(path: String, sourceFs: FileSystem, targetFs: FileSystem) {
		this.move(sourceFs.getPath(path), targetFs.getPath(path))
	}

	private fun recursivelyDelete(path: Path) {
		Files.walk(path).use { walk ->
			walk.sorted(Comparator.reverseOrder())
				.forEach {
					Files.delete(it)
				}
		}
	}
}