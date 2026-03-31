package lambdynamiclights

import org.gradle.accessors.dm.LibrariesForLibs

object Constants {
	const val NAME = "lambdynamiclights"
	const val NAMESPACE = "lambdynlights"
	const val PRETTY_NAME = "lambdynamiclightsbutitsleganinrussia"

	const val DESCRIPTION = "The most feature-complete dynamic lighting mod. Now legal in Russia!"
	const val API_DESCRIPTION = "Library to provide dynamic lighting to Minecraft through LambDynamicLights."

	const val API_ARTIFACT = "$NAME-api"

	@JvmField
	val AUTHORS = listOf("LambdAurora")

	@JvmField
	val CONTRIBUTORS = listOf("Akarys")

	const val PROJECT_LINK = "https://github.com/GameLord2011/LambDynamicLightsButItsLegalInRussia"
	const val SOURCES_LINK = "https://github.com/GameLord2011/LambDynamicLightsButItsLegalInRussia"
	const val LICENSE = "Lambda License"

	private var minecraftVersion: String? = null

	fun finalizeInit(libs: LibrariesForLibs) {
		this.minecraftVersion = libs.versions.minecraft.get()
	}
}
