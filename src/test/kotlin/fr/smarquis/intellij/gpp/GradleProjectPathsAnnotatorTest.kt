package fr.smarquis.intellij.gpp

import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.intellij.lang.annotations.Language

class GradleProjectPathsAnnotatorTest : GradleProjectPathsTestCase() {

    private fun CodeInsightTestFixture.configureGradleBuildFileWithFakeDsl(
        @Language("kotlin") text: String,
    ) = configureGradleBuildFile(
        """
        fun dependencies(body: Any) = Unit
        fun implementation(dependency: Any): Any = Unit
        fun project(path: String): Any = Unit
        """.trimIndent() + "\n\n" + text
    )

    fun testNoError_whenServiceHasNoPaths() {
        // When service has no paths, paths.isEmpty() returns true, so annotator should early return and not annotate
        seedGppService()
        myFixture.configureGradleBuildFileWithFakeDsl(
            """
            dependencies {
                implementation(project(":missing"))
            }
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false)
    }

    fun testNoError_whenPathIsKnown() {
        seedGppService(":core:data")
        myFixture.configureGradleBuildFileWithFakeDsl(
            """
            dependencies {
                implementation(project(":core:data"))
            }
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false)
    }

    fun testError_whenPathIsUnknown() {
        seedGppService(":app") // only ':app' is known
        myFixture.configureGradleBuildFileWithFakeDsl(
            """
            dependencies {
                implementation(project("<error descr="Unknown Gradle project path: :missing">:missing</error>"))
            }
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false)
    }

    fun testNoError_inKotlinFile() {
        seedGppService(":app")
        myFixture.configureKotlinFile(
            "Main.kt",
            """
            fun main() {
                val p = ":missing"
            }
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false)
    }

    fun testNoError_forTemplateStringProject() {
        seedGppService(":app")
        myFixture.configureGradleBuildFileWithFakeDsl(
            $$"""
            val variable = "missing"
            dependencies {
                implementation(project("$variable"))
            }
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false)
    }

    fun testNoError_forNonProjectCall() {
        seedGppService(":app")
        myFixture.configureGradleBuildFileWithFakeDsl(
            """
            fun notProject(path: String): Any = Unit
            dependencies {
                implementation(notProject(":missing"))
            }
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false)
    }

    fun testMultipleErrors_forMultipleUnknownPaths() {
        seedGppService(":app")
        myFixture.configureGradleBuildFileWithFakeDsl(
            """
            dependencies {
                implementation(project("<error descr="Unknown Gradle project path: :missing-one">:missing-one</error>"))
                implementation(project("<error descr="Unknown Gradle project path: :missing-two">:missing-two</error>"))
            }
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false)
    }

}
