package com.catpuppyapp.puppygit.codeeditor

import androidx.compose.ui.graphics.Color
import com.catpuppyapp.puppygit.ui.theme.Theme
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import org.eclipse.tm4e.core.registry.IThemeSource

object PLTheme {
    // follow app theme
    private val AUTO = ""
//    private val THEME_DARK = "darcula"
    private val THEME_DARK = "solarized_dark"
//    private val THEME_DARK = "abyss"
    private val THEME_LIGHT = "solarized_light"
//    private val THEME_LIGHT = "quietlight"
//    private val THEME_LIGHT = "solarized_dark"

    val THEMES = listOf(THEME_DARK, THEME_LIGHT)

    val BG_DARK = Color(0xFF131313)
//    val BG_DARK = Color(0xFF002B36)  // solarized dark, default, blue
    val BG_LIGHT = Color(0xFFF7F7F7)
//    val current = mutableStateOf(AUTO)
//
//    fun init(inDarkTheme: Boolean) {
//        current.value = if(inDarkTheme) THEME_DARK else THEME_LIGHT
//    }
//
//    fun update(inDarkTheme: Boolean) {
//        init(inDarkTheme)
//    }

    fun setTheme(inDarkTheme: Boolean) {
        val theme = if(inDarkTheme) THEME_DARK else THEME_LIGHT
        ThemeRegistry.getInstance().setTheme(theme)
    }

    fun getBackground(inDarkTheme: Boolean) = if(inDarkTheme) BG_DARK else BG_LIGHT

    fun updateThemeByAppTheme() {
        setTheme(Theme.inDarkTheme)
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
