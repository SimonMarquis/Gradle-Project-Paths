package fr.smarquis.intellij.gpp

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.isInGradleBuildFile
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.resolveProjectCallFromLeaf


class GradleProjectPathsUtilsTest : BasePlatformTestCase() {

    fun testResolveProjectCallFromLeaf() {
        val file = myFixture.configureByText(
            "build.gradle.kts",
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

    fun testIsInGradleBuildFile() {
        val file = myFixture.configureByText(
            "build.gradle.kts",
            """
            plugins { }
            dependencies {}
            """.trimIndent()
        )
        val element = file.findElementAt(0)!!
        assertTrue(element.isInGradleBuildFile())
    }

}
