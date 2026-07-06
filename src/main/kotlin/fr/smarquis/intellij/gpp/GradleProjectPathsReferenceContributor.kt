package fr.smarquis.intellij.gpp

import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.ProjectCall
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.psiGradleBuildFilePattern
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.resolveGradleBuildFile
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.resolveProjectCallFromLeaf
import org.jetbrains.kotlin.idea.base.psi.textRangeIn
import org.jetbrains.kotlin.psi.KtStringTemplateExpression


/**
 * Convert the `project(String)` `PsiElement`s to the corresponding Gradle build file to support navigation
 */
class GradleProjectReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) = registrar.registerReferenceProvider(
        psiElement(KtStringTemplateExpression::class.java).inFile(psiGradleBuildFilePattern()),
        GradleProjectReferenceProvider(),
    )
}

class GradleProjectReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> = element
        .resolveProjectCallFromLeaf()
        ?.let { GradleProjectReference(element, it) }
        ?.let { arrayOf(it) }
        ?: PsiReference.EMPTY_ARRAY
}

class GradleProjectReference(element: PsiElement, val call: ProjectCall) :
    PsiReferenceBase<PsiElement>(element, /* rangeInElement = */ call.literal.textRangeIn(element)) {
    override fun resolve() = call.resolveGradleBuildFile()
}

