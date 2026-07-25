package fr.smarquis.intellij.gpp

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import fr.smarquis.intellij.gpp.GradleProjectPathsUtils.gpp
import kotlin.io.path.Path

class GradleProjectPathsServiceTest : BasePlatformTestCase() {

    fun testGradleProjects_cacheHit() {
        val service = project.gpp()
        val customMap = mapOf(
            ":dummy" to GradleModuleDataHolder(null, stubModuleData(":dummy", Path("")), null)
        )

        service.setGradleProjectsForTest(customMap)

        assertSame("Expected cache hit on consecutive calls", customMap, service.gradleProjects())
    }

    fun testInvalidateCache_causesReload() {
        val service = project.gpp()
        val customMap = mapOf(
            ":dummy" to GradleModuleDataHolder(null, stubModuleData(":dummy", Path("")), null)
        )

        service.setGradleProjectsForTest(customMap)
        assertSame(customMap, service.gradleProjects())

        service.invalidateGradleProjectsCache()

        assertNotSame("Cache should be cleared and reloaded after invalidation", customMap, service.gradleProjects())
    }

}
