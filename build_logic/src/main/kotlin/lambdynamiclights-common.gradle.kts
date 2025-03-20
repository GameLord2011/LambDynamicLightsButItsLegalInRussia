import lambdynamiclights.Constants
import lambdynamiclights.mappings.MojangMappingsSpec
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
	id("fabric-loom")
}

// Seriously you should not worry about it, definitely not a hack.
// https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
val libs = the<LibrariesForLibs>()
Constants.finalizeInit(libs)

group = Constants.GROUP
version = "${Constants.VERSION}+${Constants.mcVersionTag()}"

loom {
	runtimeOnlyLog4j = true
}

repositories {
	mavenCentral()
	maven {
		name = "Gegy"
		url = uri("https://maven.gegy.dev")
	}
}

dependencies {
	minecraft(libs.minecraft)
}

java {
	sourceCompatibility = JavaVersion.toVersion(Constants.JAVA_VERSION)
	targetCompatibility = JavaVersion.toVersion(Constants.JAVA_VERSION)

	withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"

	options.release.set(Constants.JAVA_VERSION)
}
