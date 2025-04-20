package lambdynamiclights

import dev.lambdaurora.mcdev.api.ModUtils
import org.gradle.accessors.dm.LibrariesForLibs

object Constants {
	const val GROUP = "dev.lambdaurora.lambdynamiclights"
	const val NAME = "lambdynamiclights"
	const val NAMESPACE = "lambdynlights"
	const val PRETTY_NAME = "LambDynamicLights"
	const val VERSION = "4.2.6"
	const val JAVA_VERSION = 21

	const val DESCRIPTION = "The most feature-complete dynamic lighting mod for Fabric."
	const val API_DESCRIPTION = "Library to provide dynamic lighting to Minecraft through LambDynamicLights."

	@JvmField
	val AUTHORS = listOf("LambdAurora")

	@JvmField
	val CONTRIBUTORS = listOf("Akarys")
	const val PROJECT_LINK = "https://lambdaurora.dev/projects/lambdynamiclights"
	const val SOURCES_LINK = "https://github.com/LambdAurora/LambDynamicLights"
	const val LICENSE = "Lambda License"

	private var minecraftVersion: String? = null

	fun finalizeInit(libs: LibrariesForLibs) {
		this.minecraftVersion = libs.versions.minecraft.get()
	}

	fun mcVersion(): String {
		return this.minecraftVersion!!
	}

	fun getVersionType(): String {
		return ModUtils.fetchVersionType(this.VERSION, this.mcVersion())
	}
}
