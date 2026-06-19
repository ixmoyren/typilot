@file:Suppress("UnstableApiUsage")

package com.github.ixmoyren.typilot

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.TypilotBundle"

object TypilotBundle : DynamicBundle(BUNDLE) {
    fun String.toPlatformInfo(): PlatformInfo? {
        val parts = split('.')
        return if (parts.size >= 2) {
            PlatformInfo(
                os = parts[parts.size - 2],
                arch = parts.last()
            )
        } else null
    }

    val tinymistPlatforms: Set<PlatformInfo> by lazy {
        super.getResourceBundle().keys.toList()
            .filter { it.contains(TypilotBundle["download.tinymist.setting.prefix"]) }
            .mapNotNull { it.toPlatformInfo() }
            .toSet()
    }

    operator fun get(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) = getMessage(key, *params)

    @Suppress("unused")
    @JvmStatic
    fun messagePointer(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
        getLazyMessage(key, *params)
}
