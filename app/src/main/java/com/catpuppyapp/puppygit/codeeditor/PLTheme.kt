package com.catpuppyapp.puppygit.codeeditor

import androidx.compose.runtime.mutableStateOf
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import org.eclipse.tm4e.core.registry.IThemeSource

object PLTheme {
    // follow app theme
    private val AUTO = ""
    private val THEME_DARK = "darcula"
    private val THEME_LIGHT = "quietlight"

    val THEMES = listOf(THEME_DARK, THEME_LIGHT)

//    val current = mutableStateOf(AUTO)
//
//    fun init(inDarkTheme: Boolean) {
//        current.value = if(inDarkTheme) THEME_DARK else THEME_LIGHT
//    }
//
//    fun update(inDarkTheme: Boolean) {
//        init(inDarkTheme)
//    }

    fun applyTheme(inDarkTheme: Boolean) {
        val theme = if(inDarkTheme) THEME_DARK else THEME_LIGHT
        ThemeRegistry.getInstance().setTheme(theme)
    }


    /**
     * Load default textmate themes
     */
    /*suspend*/ fun loadDefaultTextMateThemes() /*= withContext(Dispatchers.IO)*/ {
        val themes = THEMES
        val themeRegistry = ThemeRegistry.getInstance()
        themes.forEach { name ->
            val path = "textmate/$name.json"
            themeRegistry.loadTheme(
                ThemeModel(
                    IThemeSource.fromInputStream(
                        FileProviderRegistry.getInstance().tryGetInputStream(path), path, null
                    ), name
                ).apply {
                    if (name != THEME_LIGHT) {
                        isDark = true
                    }
                }
            )
        }
    }
}
