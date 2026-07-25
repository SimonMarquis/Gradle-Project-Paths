package fr.smarquis.intellij.gpp

import com.intellij.openapi.externalSystem.model.project.ModuleData
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.intellij.lang.annotations.Language
import org.jetbrains.plugins.gradle.util.GradleConstants
import org.jetbrains.plugins.gradle.util.gradlePath
import java.nio.file.Path
import kotlin.io.path.invariantSeparatorsPathString

/**
 * Creates a minimal [ModuleData] suitable for seeding [GradleProjectPathsService] in tests.
 *
 * @param gradlePath  the Gradle project path, e.g. `":core:data"`.
 * @param externalProjectPath  the filesystem path used by [GradleProjectPathsUtils.fallbackGradleBuildFile] to locate the `build.gradle(.kts)` file.
 */
internal fun stubModuleData(
    gradlePath: String,
    externalProjectPath: Path,
): ModuleData = ModuleData(
    /* id                      = */ gradlePath,
    /* owner                   = */ GradleConstants.SYSTEM_ID,
    /* moduleTypeId            = */ "JAVA_MODULE",
    /* externalName            = */ gradlePath.substringAfterLast(':'),
    /* moduleFileDirectoryPath = */ externalProjectPath.invariantSeparatorsPathString,
    /* externalConfigPath      = */ externalProjectPath.invariantSeparatorsPathString,
).apply {
    this.gradlePath = gradlePath
}

internal fun CodeInsightTestFixture.configureGradleBuildFile(
    @Language("kotlin") text: String,
) = configureByText(/* fileName = */ "build.gradle.kts", /* text = */ text)

internal fun CodeInsightTestFixture.configureKotlinFile(
    fileName: String,
    @Language("kotlin") text: String,
) = configureByText(fileName, text)


internal fun Path.virtualFile(): VirtualFile? = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(this)
