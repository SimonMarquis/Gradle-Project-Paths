package fr.smarquis.intellij.gpp

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.externalSystem.service.project.manage.ProjectDataImportListener
import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBusConnection
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.findGradleModules

@Service(Service.Level.PROJECT)
class GradleProjectPathsService(val project: Project) : Disposable {

    private var gradleProjects: Map<String, GradleModuleDataHolder>? = null
    private var connection: MessageBusConnection?

    init {
        connection = project.messageBus.connect().apply {
            subscribe(
                ProjectDataImportListener.TOPIC,
                object : ProjectDataImportListener {
                    override fun onImportFinished(projectPath: String?) {
                        invalidateGradleProjectsCache()
                    }
                }
            )
        }
    }

    /**
     * Gets all available Gradle projects, keyed by path.
     * Relies on a simple cached field ([gradleProjects]) to avoid recomputing all modules everytime.
     */
    fun gradleProjects() =
        gradleProjects ?: loadGradleProjects().also { gradleProjects = it }

    private fun loadGradleProjects() = project
        .findGradleModules()
        .map { GradleModuleDataHolder.from(project, it) }
        .associateBy { it.path }
        .also { thisLogger().info("Found ${it.size} Gradle Project paths: ${it.keys.joinToString(limit = 10)}") }

    internal fun invalidateGradleProjectsCache() {
        thisLogger().info("Gradle Project Paths invalidation requested!")
        gradleProjects = null
    }

    override fun dispose() {
        connection?.disconnect()
        connection = null
    }
}