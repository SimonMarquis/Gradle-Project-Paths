package fr.smarquis.intellij.gpp

import com.intellij.codeInsight.daemon.GutterMark
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.gpp
import icons.GradleIcons
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories

class GradleProjectPathsLineMarkerProviderTest : GradleProjectPathsTestCase() {

    private fun List<GutterMark>.gppGutter() = firstOrNull {
        it.tooltipText == GradleProjectPathsBundle["gpp.gutter.navigation-tooltip"]
    }

    fun testGutterShown_forKnownPathWithExistingBuildFile() {
        val buildFile = tempDir.resolve("core/build.gradle.kts").createParentDirectories().createFile()
        assertNotNull("VirtualFile for physical build.gradle.kts must be found", buildFile.virtualFile())

        // Seed with a specific external project path pointing to the parent folder of build.gradle.kts
        val moduleData = stubModuleData(":core", buildFile.parent)
        myFixture.project.gpp().setGradleProjectsForTest(
            mapOf(":core" to GradleModuleDataHolder(null, moduleData, null))
        )

        myFixture.configureGradleBuildFile(
            """
            dependencies {
                implementation(project(":core"))
            }
            """.trimIndent()
        )

        val gppGutter = myFixture.findAllGutters().gppGutter()
        assertNotNull("GPP Gutter icon should be present", gppGutter)
        assertEquals(GradleIcons.GradleNavigate, gppGutter?.icon)
    }

    fun testNoGutter_forUnknownPath() {
        seedGppService(":app")

        myFixture.configureGradleBuildFile(
            """
            dependencies {
                implementation(project(":missing"))
            }
            """.trimIndent()
        )

        assertNull("GPP Gutter icon should not be present for unknown paths", myFixture.findAllGutters().gppGutter())
    }

    fun testNoGutter_outsideGradleFile() {
        val buildFile = tempDir.resolve("core/build.gradle.kts").createParentDirectories().createFile()
        assertNotNull("VirtualFile for physical build.gradle.kts must be found", buildFile.virtualFile())

        val moduleData = stubModuleData(":core", buildFile.parent)
        myFixture.project.gpp().setGradleProjectsForTest(
            mapOf(":core" to GradleModuleDataHolder(null, moduleData, null))
        )

        myFixture.configureKotlinFile(
            "Main.kt",
            """
            fun main() {
                val p = ":core"
            }
            """.trimIndent()
        )

        assertNull(
            "GPP Gutter icon should not be present outside Gradle build files",
            myFixture.findAllGutters().gppGutter()
        )
    }

}
