package fr.smarquis.intellij.gpp

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.GradleProjectPathsBundle"

object GradleProjectPathsBundle : DynamicBundle(BUNDLE) {
    operator fun get(
        @PropertyKey(resourceBundle = BUNDLE) key: String,
        vararg params: Any
    ): String = getMessage(key, *params)
}
