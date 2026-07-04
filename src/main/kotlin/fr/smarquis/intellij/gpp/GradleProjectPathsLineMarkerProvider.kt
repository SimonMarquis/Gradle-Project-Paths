package fr.smarquis.intellij.gpp

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiElement
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.gpp
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.isInGradleBuildFile
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.leaf
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.resolveGradleBuildFile
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.resolveProjectCall
import icons.GradleIcons

class GradleProjectPathsLineMarkerProvider : RelatedItemLineMarkerProvider() {
    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        if (!element.isInGradleBuildFile()) return
        val resolved = element.resolveProjectCall() ?: return

        val paths = element.project.gpp().gradleProjects()
        if (paths.isEmpty() || resolved.path !in paths) return

        NavigationGutterIconBuilder.create(GradleIcons.GradleNavigate)
            .setTarget(resolved.resolveGradleBuildFile() ?: return)
            .setTooltipText(GradleProjectPathsBundle["gpp.gutter.navigation-tooltip"])
            .createLineMarkerInfo(resolved.literal.leaf() ?: resolved.literal)
            .let(result::add)
    }
}
