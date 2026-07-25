package fr.smarquis.intellij.gpp

import com.intellij.codeInsight.daemon.ProblemHighlightFilter
import com.intellij.psi.PsiFile
import com.intellij.testFramework.ExtensionTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.gpp
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

/**
 * Base test class providing helper methods.
 */
abstract class GradleProjectPathsTestCase : BasePlatformTestCase() {

    lateinit var tempDir: Path

    override fun setUp() {
        super.setUp()
        tempDir = Files.createTempDirectory("gpp-test")
        // Mask the ProblemHighlightFilter EP with a filter that always returns true,
        // allowing .gradle.kts files to be highlighted and annotated during test execution.
        ExtensionTestUtil.maskExtensions(
            pointName = ProblemHighlightFilter.EP_NAME,
            newExtensions = listOf(object : ProblemHighlightFilter() {
                override fun shouldHighlight(file: PsiFile): Boolean = true
            }),
            parentDisposable = testRootDisposable
        )
    }

    override fun tearDown() {
        try {
            @OptIn(ExperimentalPathApi::class)
            tempDir.deleteRecursively()
        } catch (e: Throwable) {
            addSuppressedException(e)
        } finally {
            super.tearDown()
        }
    }

    protected fun seedGppService(vararg paths: String) {
        val basePath = myFixture.project.basePath ?: ""
        // Use a dummy directory under the project base path so VFS root access is allowed,
        // but no build.gradle.kts file is found there.
        val dummyPath = Paths.get("$basePath/non_existent_gradle_project")
        myFixture.project.gpp().setGradleProjectsForTest(
            paths.associateWith { GradleModuleDataHolder(null, stubModuleData(it, dummyPath), null) }
        )
    }
}