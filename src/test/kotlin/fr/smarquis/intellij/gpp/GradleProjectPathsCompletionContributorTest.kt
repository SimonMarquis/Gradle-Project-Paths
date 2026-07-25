package fr.smarquis.intellij.gpp

import com.intellij.codeInsight.lookup.LookupElementPresentation
import icons.GradleIcons

class GradleProjectPathsCompletionContributorTest : GradleProjectPathsTestCase() {

    fun testCompletion_insideEmptyProjectCall() {
        seedGppService(":app", ":core:data")
        myFixture.configureGradleBuildFile(
            """
            dependencies {
                implementation(project("<caret>"))
            }
            """.trimIndent()
        )
        val elements = myFixture.completeBasic()
        assertNotNull(elements)
        val lookupStrings = elements.map { it.lookupString }
        assertContainsElements(lookupStrings, ":app", ":core:data")
    }

    fun testCompletion_insidePartialProjectCall() {
        // Seed multiple matching paths to avoid auto-insertion of a single matching suggestion.
        seedGppService(":app", ":core:data", ":core:domain")
        myFixture.configureGradleBuildFile(
            """
            dependencies {
                implementation(project(":cor<caret>"))
            }
            """.trimIndent()
        )
        val elements = myFixture.completeBasic()
        assertNotNull("Expected multiple completion variants, not auto-inserted", elements)
        val lookupStrings = elements.map { it.lookupString }
        assertContainsElements(lookupStrings, ":core:data", ":core:domain")
        assertFalse(lookupStrings.contains(":app"))
    }

    fun testCompletion_noSuggestionsOutsideProjectCall() {
        seedGppService(":app", ":core:data")
        myFixture.configureGradleBuildFile(
            """
            dependencies {
                implementation("<caret>")
            }
            """.trimIndent()
        )
        val elements = myFixture.completeBasic()
        // Completion might return standard strings or other contributors, but not GPP ones
        if (elements != null) {
            val lookupStrings = elements.map { it.lookupString }
            assertFalse(lookupStrings.contains(":app"))
            assertFalse(lookupStrings.contains(":core:data"))
        }
    }

    fun testCompletion_noSuggestionsInKotlinFile() {
        seedGppService(":app", ":core:data")
        myFixture.configureKotlinFile(
            "Main.kt",
            """
            fun main() {
                val p = "<caret>"
            }
            """.trimIndent()
        )
        val elements = myFixture.completeBasic()
        if (elements != null) {
            val lookupStrings = elements.map { it.lookupString }
            assertFalse(lookupStrings.contains(":app"))
            assertFalse(lookupStrings.contains(":core:data"))
        }
    }

    fun testCompletion_noSuggestionsWhenServiceEmpty() {
        seedGppService()
        myFixture.configureGradleBuildFile(
            """
            dependencies {
                implementation(project("<caret>"))
            }
            """.trimIndent()
        )
        val elements = myFixture.completeBasic()
        assertTrue(elements == null || elements.isEmpty())
    }

    fun testCompletion_lookupElementPresentation() {
        seedGppService(":app")
        myFixture.configureGradleBuildFile(
            """
            dependencies {
                implementation(project("<caret>"))
            }
            """.trimIndent()
        )
        val elements = myFixture.completeBasic()
        assertNotNull(elements)
        val appElement = elements.first { it.lookupString == ":app" }
        val presentation = LookupElementPresentation()
        appElement.renderElement(presentation)

        assertEquals(GradleIcons.GradleFile, presentation.icon)
        assertEquals(GradleProjectPathsBundle["gpp.contributor-label"], presentation.typeText)
    }

}
