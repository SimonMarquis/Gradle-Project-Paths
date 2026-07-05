package fr.smarquis.intellij.gpp

import com.intellij.openapi.externalSystem.model.project.ModuleData
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.plugins.gradle.service.project.GradleModuleDataIndex
import org.jetbrains.plugins.gradle.util.GradleModuleData
import org.jetbrains.plugins.gradle.util.GradleUtil
import org.jetbrains.plugins.gradle.util.gradlePath

@Suppress("UnstableApiUsage")
class GradleModuleDataHolder(
    val module: Module?,
    val moduleData: ModuleData,
    val gradleModuleData: GradleModuleData?,
) {
    val path: String = moduleData.gradlePath
    val buildScriptFile: VirtualFile? = module?.let(GradleUtil::getGradleBuildScriptSource)

    companion object {
        fun from(project: Project, moduleData: ModuleData): GradleModuleDataHolder {
            val module = GradleUtil.findGradleModule(project, moduleData)
            val gradleModuleData = module?.let(GradleModuleDataIndex::findGradleModuleData)
            return GradleModuleDataHolder(module, moduleData, gradleModuleData)
        }
    }
}
