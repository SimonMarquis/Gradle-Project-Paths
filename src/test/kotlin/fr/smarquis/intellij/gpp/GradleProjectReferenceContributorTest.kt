package fr.smarquis.intellij.gpp

import com.intellij.psi.PsiManager
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.gpp
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories

class GradleProjectReferenceContributorTest : GradleProjectPathsTestCase() {

    fun testReference_existsInsideProjectCall() {
        seedGppService(":core")
        val file = myFixture.configureGradleBuildFile(
            """
            dependencies {
                implementation(project(":core"))
            }
            """.trimIndent()
        )
        val offset = file.text.indexOf(":core")
        val ref = file.findReferenceAt(offset)
        assertNotNull("Expected a reference at :core", ref)
        assertTrue("Reference should be a GradleProjectReference", ref is GradleProjectReference)
        assertEquals(":core", (ref as GradleProjectReference).call.path)
    }

    fun testNoReference_outsideProjectCall() {
        seedGppService(":core")
        val file = myFixture.configureGradleBuildFile(
            """
            dependencies {
                implementation(":core")
            }
            """.trimIndent()
        )
        val offset = file.text.indexOf(":core")
        val ref = file.findReferenceAt(offset)
        // Should not find a GradleProjectReference here
        assertTrue(ref == null || ref !is GradleProjectReference)
    }

    fun testNoReference_outsideGradleFile() {
        seedGppService(":core")
        val file = myFixture.configureKotlinFile(
            "Main.kt",
            """
            fun main() {
                val p = ":core"
            }
            """.trimIndent()
        )
        val offset = file.text.indexOf(":core")
        val ref = file.findReferenceAt(offset)
        assertTrue(ref == null || ref !is GradleProjectReference)
    }

    fun testNoReference_forTemplateString() {
        seedGppService(":core")
        val file = myFixture.configureGradleBuildFile(
            $$"""
            val name = "core"
            dependencies {
                implementation(project("$name"))
            }
            """.trimIndent()
        )
        val file2 = myFixture.configureGradleBuildFile(
            $$"""val name = "core"; val p = project("$name")"""
        )
        val offset = file2.text.indexOf("name")
        val ref = file2.findReferenceAt(offset)
        assertTrue(ref == null || ref !is GradleProjectReference)
    }

    fun testReference_resolvesNull_whenPathUnknown() {
        seedGppService(":core")
        val file = myFixture.configureGradleBuildFile(
            """
            dependencies {
                implementation(project(":unknown"))
            }
            """.trimIndent()
        )
        val offset = file.text.indexOf(":unknown")
        val ref = file.findReferenceAt(offset)
        // An unknown path should still return a reference but resolve to null
        assertNotNull(ref)
        assertTrue(ref is GradleProjectReference)
        assertNull(ref?.resolve())
    }

    fun testReference_resolvesToBuildFile_whenPathKnownAndFileExists() {
        val buildFile = tempDir.resolve("core/build.gradle.kts").createParentDirectories().createFile()

        // Seed with coreDir absolute path (where fallbackGradleBuildFile looks for build.gradle.kts)
        val moduleData = stubModuleData(":core", buildFile.parent)
        myFixture.project.gpp().setGradleProjectsForTest(
            mapOf(":core" to GradleModuleDataHolder(null, moduleData, null))
        )

        val file = myFixture.configureGradleBuildFile(
            """
            dependencies {
                implementation(project(":core"))
            }
            """.trimIndent()
        )
        val offset = file.text.indexOf(":core")
        val ref = file.findReferenceAt(offset)
        assertNotNull(ref)
        assertTrue(ref is GradleProjectReference)

        val resolved = ref?.resolve()
        assertNotNull("Reference should resolve to target build file", resolved)
        val expectedPsiFile = PsiManager.getInstance(project).findFile(buildFile.virtualFile()!!)
        assertEquals(expectedPsiFile, resolved)
    }

}
