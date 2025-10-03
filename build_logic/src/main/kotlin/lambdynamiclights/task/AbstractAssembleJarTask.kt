package lambdynamiclights.task

import dev.yumi.commons.function.YumiPredicates
import org.gradle.jvm.tasks.Jar
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.nio.file.attribute.PosixFilePermissions
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.function.Predicate
import javax.inject.Inject


abstract class AbstractAssembleJarTask @Inject constructor() : Jar() {
	protected fun openJar(path: Path): FileSystem {
		return FileSystems.newFileSystem(path, mapOf("enablePosixFileAttributes" to "true"))
	}

	protected fun createDirectories(path: Path) {
		if (Files.exists(path)) {
			return
		} else {
			this.createDirectories(path.toAbsolutePath().parent)
			Files.createDirectory(path)
			Files.setPosixFilePermissions(path, DEFAULT_DIR_MODE)
			Files.getFileAttributeView(path, BasicFileAttributeView::class.java)
				.setTimes(ZIP_EPOCH, ZIP_EPOCH, ZIP_EPOCH)
		}
	}

	protected fun writeString(path: Path, csq: CharSequence) {
		Files.writeString(path, csq)
		Files.setPosixFilePermissions(path, DEFAULT_FILE_MODE)
		Files.getFileAttributeView(path, BasicFileAttributeView::class.java)
			.setTimes(ZIP_EPOCH, ZIP_EPOCH, ZIP_EPOCH)
	}

	protected fun copy(source: Path, target: Path, vararg options: CopyOption) {
		Files.copy(source, target, *options)
		Files.setPosixFilePermissions(target, DEFAULT_FILE_MODE)
		Files.getFileAttributeView(target, BasicFileAttributeView::class.java)
			.setTimes(ZIP_EPOCH, ZIP_EPOCH, ZIP_EPOCH)
	}

	protected fun copy(source: Path, target: Path, predicate: Predicate<Path>) {
		if (target.parent != null) {
			this.createDirectories(target.parent)
		}

		if (Files.isRegularFile(source)) {
			if (predicate.test(source))
				this.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
			return
		}

		Files.walkFileTree(source, object : SimpleFileVisitor<Path>() {
			fun resolve(subSource: Path): Path {
				val relative = source.relativize(subSource)
				return target.resolve(relative)
			}

			override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
				if (!predicate.test(dir)) return FileVisitResult.SKIP_SUBTREE
				createDirectories(this.resolve(dir))
				return FileVisitResult.CONTINUE
			}

			override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
				if (predicate.test(file)) {
					copy(file, this.resolve(file), StandardCopyOption.REPLACE_EXISTING)
				}
				return FileVisitResult.CONTINUE
			}
		})
	}

	protected fun copy(path: String, sourceFs: FileSystem, targetFs: FileSystem, predicate: Predicate<Path> = YumiPredicates.alwaysTrue()) {
		this.copy(sourceFs.getPath(path), targetFs.getPath(path), predicate)
	}

	protected fun move(source: Path, target: Path) {
		if (target.parent != null) {
			this.createDirectories(target.parent)
		}

		if (Files.isRegularFile(source)) {
			Files.move(source, target)
			Files.setPosixFilePermissions(target, DEFAULT_FILE_MODE)
			Files.getFileAttributeView(target, BasicFileAttributeView::class.java)
				.setTimes(ZIP_EPOCH, ZIP_EPOCH, ZIP_EPOCH)
			return
		}

		Files.walkFileTree(source, object : SimpleFileVisitor<Path>() {
			fun resolve(subSource: Path): Path {
				val relative = source.relativize(subSource)
				return target.resolve(relative)
			}

			override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
				createDirectories(this.resolve(dir))
				return FileVisitResult.CONTINUE
			}

			override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
				val target = this.resolve(file)
				Files.move(file, target)
				Files.setPosixFilePermissions(target, DEFAULT_FILE_MODE)
				Files.getFileAttributeView(target, BasicFileAttributeView::class.java)
					.setTimes(ZIP_EPOCH, ZIP_EPOCH, ZIP_EPOCH)
				return FileVisitResult.CONTINUE
			}
		})
		this.recursivelyDelete(source)
	}

	protected fun move(path: String, sourceFs: FileSystem, targetFs: FileSystem) {
		this.move(sourceFs.getPath(path), targetFs.getPath(path))
	}

	protected fun recursivelyDelete(path: Path) {
		Files.walk(path).use { walk ->
			walk.sorted(Comparator.reverseOrder())
				.forEach {
					Files.delete(it)
				}
		}
	}

	companion object {
		val DEFAULT_DIR_MODE = PosixFilePermissions.fromString("rwxr-xr-x")
		val DEFAULT_FILE_MODE = PosixFilePermissions.fromString("rw-r--r--")
		val ZIP_EPOCH: FileTime = FileTime.from(
			OffsetDateTime.of(
				1980, 2, 1,
				0, 0, 0, 0,
				ZoneOffset.ofHours(1)
			).toInstant()
		)
	}
}