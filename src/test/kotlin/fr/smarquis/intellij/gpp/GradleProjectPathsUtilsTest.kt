package fr.smarquis.intellij.gpp

import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.isInGradleBuildFile
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.leaf
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.psiGradleBuildFilePattern
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.resolveProjectCall
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.resolveProjectCallFromLeaf
import org.jetbrains.kotlin.psi.KtCallExpression

class GradleProjectPathsUtilsTest : BasePlatformTestCase() {

    fun testResolveProjectCallFromLeaf() {
        val file = myFixture.configureGradleBuildFile(
            """
            plugins { }
            dependencies {
                implementation(project(":core:data"))
                implementation("org.jetbrains:kotlin")
            }
            """.trimIndent()
        )

        // Find the project string ":core:data"
        val element1 = file.findElementAt(file.text.indexOf(":core:data"))
        assertNotNull(
            "Should be inside project() call",
            element1?.resolveProjectCallFromLeaf()
        )

        // Find "org.jetbrains:kotlin"
        val element2 = file.findElementAt(file.text.indexOf("org.jetbrains:kotlin"))
        assertNull(
            "Should not be inside project() call",
            element2?.resolveProjectCallFromLeaf()
        )
    }

    fun testResolveProjectCall_returnsNullForTemplateString() {
        val file = myFixture.configureGradleBuildFile(
            $$"""
            val variable = "core:data"
            dependencies {
                implementation(project("$variable"))
            }
            """.trimIndent()
        )
        val offset = file.text.indexOf("project")
        val call = file.findElementAt(offset)!!.parentOfType<KtCallExpression>()
        assertNotNull("Call expression should be found", call)
        assertNull("Should not resolve to ProjectCall for a template string with variable reference", call?.resolveProjectCall())
    }

    fun testResolveProjectCall_returnsNullForNonProjectCall() {
        val file = myFixture.configureGradleBuildFile(
            """
            dependencies {
                implementation(notProject(":core"))
            }
            """.trimIndent()
        )
        val offset = file.text.indexOf("notProject")
        val call = file.findElementAt(offset)!!.parentOfType<KtCallExpression>()
        assertNotNull("Call expression should be found", call)
        assertNull("Should not resolve to ProjectCall for non-project call name", call?.resolveProjectCall())
    }

    fun testResolveProjectCall_returnsNullForNoArgs() {
        val file = myFixture.configureGradleBuildFile(
            """
            dependencies {
                implementation(project())
            }
            """.trimIndent()
        )
        val call = file.findElementAt(file.text.indexOf("project"))!!.parentOfType<KtCallExpression>()!!
        assertNull("Should not resolve to ProjectCall with no arguments", call.resolveProjectCall())
    }

    fun testIsInGradleBuildFile() {
        val ktsFile = myFixture.configureGradleBuildFile(
            """
            plugins {}
            dependencies {}
            """.trimIndent()
        )
        assertTrue(ktsFile.findElementAt(0)!!.isInGradleBuildFile())

        val ktFile = myFixture.configureKotlinFile(
            "Main.kt",
            """
            fun main() {}
            """.trimIndent()
        )
        assertFalse(ktFile.findElementAt(0)!!.isInGradleBuildFile())
    }

    fun testGradleBuildFilePattern() {
        val pattern = psiGradleBuildFilePattern()

        val ktsFile = myFixture.configureGradleBuildFile("")
        assertTrue(pattern.accepts(ktsFile))

        val settingsFile = myFixture.configureByText("settings.gradle.kts", "")
        assertTrue(pattern.accepts(settingsFile))

        val ktFile = myFixture.configureByText("Main.kt", "")
        assertFalse(pattern.accepts(ktFile))

        val groovyFile = myFixture.configureByText("build.gradle", "")
        assertFalse(pattern.accepts(groovyFile))
    }

    fun testLeaf_findsFirstLeafPsiElement() {
        val file = myFixture.configureGradleBuildFile(
            """
            dependencies {
                implementation(project(":core"))
            }
            """.trimIndent()
        )
        val projectCall = file.findElementAt(file.text.indexOf("project"))!!.parentOfType<KtCallExpression>()!!
        val leafElement = projectCall.leaf()
        assertNotNull("Should find a leaf element", leafElement)
        assertTrue("Should be LeafPsiElement", leafElement is LeafPsiElement)
        assertEquals("project", leafElement?.text)
    }

}
