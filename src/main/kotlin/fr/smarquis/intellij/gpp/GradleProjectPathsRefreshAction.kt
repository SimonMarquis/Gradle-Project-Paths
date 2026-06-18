package fr.smarquis.intellij.gpp

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.intention.PriorityAction.Priority.LOW
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.gpp

/**
 * Action to invalidate Gradle Project Paths cache.
 */
class GradleProjectPathsRefreshAction : AnAction(), PriorityAction {

    override fun actionPerformed(event: AnActionEvent) {
        event.project?.gpp()?.invalidateGradleProjectsCache()
    }

    override fun getPriority() = LOW

}