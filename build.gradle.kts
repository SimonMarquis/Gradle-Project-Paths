import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.AndroidStudio
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.IntellijIdea
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog")
}

intellijPlatform {
    pluginConfiguration {
        name = "Gradle Project Paths"
    }
}

val ideTarget = providers.gradleProperty("ideTarget")
    .map(IntelliJPlatformType::valueOf)
    .orElse(IntellijIdea)

dependencies {
    testImplementation("junit:junit:4.13.2")
    intellijPlatform {
        when (val target = ideTarget.get()) {
            // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
            IntellijIdea -> intellijIdea("2025.2.6.2")
            // https://plugins.jetbrains.com/docs/intellij/android-studio-releases-list.html
            AndroidStudio -> androidStudio("2026.1.1.8")
            else -> throw GradleException("Unsupported ideTarget: $target")
        }
        testFramework(TestFrameworkType.Platform)
        bundledPlugins(
            "org.jetbrains.kotlin",
            "com.intellij.gradle",
        )
    }
}
