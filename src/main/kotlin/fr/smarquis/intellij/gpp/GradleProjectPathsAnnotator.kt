package fr.smarquis.intellij.gpp

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity.ERROR
import com.intellij.psi.PsiElement
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.gpp
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.isInGradleBuildFile
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.resolveProjectCall


/**
 * Annotate unknown project paths as errors.
 */
class GradleProjectPathsAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (!element.isInGradleBuildFile()) return
        val resolved = element.resolveProjectCall() ?: return

        val paths = element.project.gpp().gradleProjects()
        if (paths.isEmpty() || resolved.path in paths) return

        holder.newAnnotation(ERROR, GradleProjectPathsBundle["gpp.unknown-path", resolved.path])
            .range(resolved.literal)
            .create()
    }
}
