package fr.smarquis.intellij.gpp

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.externalSystem.model.project.ModuleData
import com.intellij.openapi.externalSystem.service.project.manage.ProjectDataImportListener
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBusConnection
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.findGradleModules
import org.jetbrains.plugins.gradle.service.project.GradleModuleDataIndex
import org.jetbrains.plugins.gradle.util.GradleModuleData
import org.jetbrains.plugins.gradle.util.GradleUtil
import org.jetbrains.plugins.gradle.util.gradlePath

@Suppress("UnstableApiUsage")
@Service(Service.Level.PROJECT)
class GradleProjectPathsService(val project: Project) : Disposable {

    private var gradleProjects: Map<String, Triple<ModuleData, Module?, GradleModuleData?>>? = null
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
        .associateBy(
            keySelector = { it.gradlePath },
            valueTransform = {
                val module = GradleUtil.findGradleModule(project, it)
                val gradleModuleData = module?.let(GradleModuleDataIndex::findGradleModuleData)
                Triple(it, module, gradleModuleData)
            },
        )
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