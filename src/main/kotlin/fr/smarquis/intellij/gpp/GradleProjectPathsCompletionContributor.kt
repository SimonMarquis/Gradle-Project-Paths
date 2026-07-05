package fr.smarquis.intellij.gpp

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.gpp
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.resolveProjectCallFromLeaf
import icons.GradleIcons

/**
 * Provide Gradle Project path completion, on user request.
 */
class GradleProjectPathsCompletionContributor : CompletionContributor() {

    init {
        extend(
            /* type = */ CompletionType.BASIC,
            /* place = */ PlatformPatterns.psiElement().inFile(GradleProjectPathsUtils.psiGradleBuildFilePattern()),
            /* provider = */ ProjectPathCompletionProvider()
        )
    }

    private class ProjectPathCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            parameters.position.resolveProjectCallFromLeaf() ?: return
            parameters.position.project.gpp().gradleProjects()
                .filterValues { parameters.originalFile.virtualFile != it.buildScriptFile } // Remove self
                .keys.sorted()
                .map {
                    LookupElementBuilder.create(it)
                        .withIcon(GradleIcons.GradleFile)
                        .run { if (it == ":") withTailText("(root)") else this }
                        .withTypeText(GradleProjectPathsBundle["gpp.contributor-label"])
                }
                .let(result::addAllElements)
            result.stopHere()
        }
    }

}
