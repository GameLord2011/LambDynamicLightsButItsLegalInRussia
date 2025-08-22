package lambdynamiclights

import dev.lambdaurora.mcdev.api.ModUtils
import dev.lambdaurora.mcdev.api.VersionType
import org.gradle.accessors.dm.LibrariesForLibs

object Constants {
	const val NAME = "lambdynamiclights"
	const val NAMESPACE = "lambdynlights"
	const val PRETTY_NAME = "LambDynamicLightsButWithoutThePrideFlags"
	const val VERSION = "4.3.2"
	const val JAVA_VERSION = 21

	const val DESCRIPTION = "It's LambDynamicLights, but without the pride flags in the config screen."
	const val API_DESCRIPTION = "Library to provide dynamic lighting to Minecraft through LambDynamicLights."

	@JvmField
	val AUTHORS = listOf("LambdAurora", "GameLord2011")

	@JvmField
	val CONTRIBUTORS = listOf("Akarys")

	const val PROJECT_LINK = "https://lambdaurora.dev/projects/lambdynamiclights"
	const val SOURCES_LINK = "https://github.com/LambdAurora/LambDynamicLights"
	const val LICENSE = "Lambda License"

	@JvmField
	val COMPATIBLE_MC_VERSIONS = listOf("1.21.7", "1.21.6")

	private var minecraftVersion: String? = null

	fun finalizeInit(libs: LibrariesForLibs) {
		this.minecraftVersion = libs.versions.minecraft.get()
	}

	fun mcVersion(): String {
		return this.minecraftVersion!!
	}

	fun getVersionType(): VersionType {
		return ModUtils.getVersionType(this.VERSION, this.mcVersion())
		//return VersionType.RELEASE
	}
}
