package fr.smarquis.intellij.gpp

import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.project.ModuleData
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.plugins.gradle.util.GradleConstants
import org.jetbrains.plugins.gradle.util.GradleUtil

object GradleProjectPathsUtils {

    //region Project call resolver
    class ProjectCall(val call: PsiElement, val literal: PsiElement, val path: String)

    fun PsiElement.resolveProjectCallFromLeaf(): ProjectCall? =
        parentOfType<KtCallExpression>(withSelf = false)?.resolveProjectCall()

    fun PsiElement.resolveProjectCall(): ProjectCall? {
        val call = this as? KtCallExpression ?: return null
        if (call.calleeExpression?.text != "project") return null
        if ((call.calleeExpression as? KtNameReferenceExpression)?.getReferencedName() != "project") return null

        // analyze(call) {
        //     val call = call.resolveToCall()?.singleFunctionCallOrNull()
        //     val symbol = call?.symbol
        //     symbol?.callableId?.let { it.packageName.asString() to it.callableName.asString() }
        //     //first = "org.gradle.kotlin.dsl"
        //     //second = "project"
        // }

        val argExpr = call.valueArguments.firstOrNull()
            ?.getArgumentExpression() as? KtStringTemplateExpression
            ?: return null

        val literal = argExpr.entries
            .singleOrNull() as? KtLiteralStringTemplateEntry
            ?: return null

        return ProjectCall(call, literal, literal.text)
    }
    //endregion

    //region Gradle Paths
    fun Project.findGradleModules(): List<ModuleData> {
        val gradleNode = ExternalSystemApiUtil.findProjectNode(
            /* project = */this,
            /* systemId = */ GradleConstants.SYSTEM_ID,
            /* projectPath = */ basePath ?: return emptyList()
        ) ?: return emptyList()
        return ExternalSystemApiUtil.findAll(
            /* parent = */ gradleNode,
            /* key = */ ProjectKeys.MODULE
        ).map { it.data }
    }
    //endregion

    //region Gradle File paths
    fun ProjectCall.resolveGradleBuildFile(): PsiFile? {
        val (moduleData, module, _) = call.project.gpp().gradleProjects()[path]
            ?: return null
        val buildScriptFile = module?.let(GradleUtil::getGradleBuildScriptSource)
            ?: return fallbackGradleBuildFile(moduleData)
        return PsiManager.getInstance(call.project).findFile(buildScriptFile)
    }

    fun ProjectCall.fallbackGradleBuildFile(moduleData: ModuleData): PsiFile? {
        val path = moduleData.linkedExternalProjectPath
        val buildFile = VirtualFileManager.getInstance().findFileByUrl("file://$path/build.gradle.kts")
            ?: VirtualFileManager.getInstance().findFileByUrl("file://$path/build.gradle")
            ?: return null
        return PsiManager.getInstance(call.project).findFile(buildFile)
    }
    //endregion

    //region Misc
    fun PsiElement.isInGradleBuildFile(): Boolean = containingFile.name.endsWith(".gradle.kts")
    fun PsiElement.leaf(): LeafPsiElement? = PsiTreeUtil.findChildOfType(this, LeafPsiElement::class.java)

    fun psiGradleBuildFilePattern() = PlatformPatterns.psiFile().withName(psiGradleBuildFileNamePattern())
    private fun psiGradleBuildFileNamePattern() = PlatformPatterns.string().with(GradleBuildFilePattern)

    private object GradleBuildFilePattern : PatternCondition<String>("Gradle Build File") {
        override fun accepts(t: String, context: ProcessingContext?): Boolean = t.endsWith(".gradle.kts")
    }

    inline fun <reified T> Project.service() = getService(T::class.java)
    fun Project.gpp() = service<GradleProjectPathsService>()
    //endregion

}



