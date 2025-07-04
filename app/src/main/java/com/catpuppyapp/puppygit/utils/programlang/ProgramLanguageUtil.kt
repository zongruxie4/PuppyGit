package com.catpuppyapp.puppygit.utils.programlang

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Typeface
import io.github.dingyi222666.monarch.languages.JavaLanguage
import io.github.dingyi222666.monarch.languages.KotlinLanguage
import io.github.dingyi222666.monarch.languages.PythonLanguage
import io.github.dingyi222666.monarch.languages.TypescriptLanguage
import io.github.rosemoe.sora.langs.monarch.MonarchColorScheme
import io.github.rosemoe.sora.langs.monarch.registry.MonarchGrammarRegistry
import io.github.rosemoe.sora.langs.monarch.registry.dsl.monarchLanguages
import io.github.rosemoe.sora.langs.monarch.registry.model.ThemeSource
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.dsl.languages
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import org.eclipse.tm4e.core.registry.IThemeSource

object ProgramLanguageUtil {
    var typeface: Typeface? = null
    var testLanguageJava: TextMateLanguage? = null


    fun init(assets: AssetManager, applicationContext: Context) {

        typeface = Typeface.createFromAsset(assets, "JetBrainsMono-Regular.ttf")

        // Load textmate themes and grammars
        setupTextmate(applicationContext)

        // Load monarch themes and grammars
        setupMonarch(applicationContext)
        // Before using Textmate Language, TextmateColorScheme should be applied
        ensureTextmateTheme()


//        // Set editor language to textmate Java
        testLanguageJava = TextMateLanguage.create(
            "source.java", loadDefaultTextMateLanguages(), loadDefaultTextMateThemes(), false
        )



        println("ctheme::::"+io.github.rosemoe.sora.langs.monarch.registry.ThemeRegistry.currentTheme.name)



    }
    /**
     * Ensure the editor uses a [TextMateColorScheme]
     */
    private fun ensureTextmateTheme() {
//        TextMateColorScheme.create()
    }


    /**
     * Setup Textmate. Load our grammars and themes from assets
     */
    private fun setupTextmate(applicationContext: Context) {
        // Add assets file provider so that files in assets can be loaded
        FileProviderRegistry.getInstance().addFileProvider(
            AssetsFileResolver(
                applicationContext.assets // use application context
            )
        )
        loadDefaultTextMateThemes()
        loadDefaultTextMateLanguages()
    }


    /**
     * Load default textmate themes
     */
    private /*suspend*/ fun loadDefaultTextMateThemes():ThemeRegistry /*= withContext(Dispatchers.IO)*/ {
        val themes = arrayOf("darcula", "abyss", "quietlight", "solarized_dark")
        val themeRegistry = ThemeRegistry.getInstance()
        themes.forEach { name ->
            val path = "textmate/$name.json"
            themeRegistry.loadTheme(
                ThemeModel(
                    IThemeSource.fromInputStream(
                        FileProviderRegistry.getInstance().tryGetInputStream(path), path, null
                    ), name
                ).apply {
                    if (name != "darcula") {
                        isDark = true
                    }
                }
            )
        }

        themeRegistry.setTheme("darcula")

        return themeRegistry
    }

    /**
     * Load default languages from JSON configuration
     *
     * @see loadDefaultLanguagesWithDSL Load by Kotlin DSL
     */
    private /*suspend*/ fun loadDefaultTextMateLanguages():GrammarRegistry /*= withContext(Dispatchers.Main)*/ {
        return GrammarRegistry.getInstance().apply { loadGrammars("textmate/languages.json") }
    }

    /**
     * Setup monarch. Load our grammars and themes from assets
     */
    private fun setupMonarch(applicationContext: Context) {
        // Add assets file provider so that files in assets can be loaded
        io.github.rosemoe.sora.langs.monarch.registry.FileProviderRegistry.addProvider(
            io.github.rosemoe.sora.langs.monarch.registry.provider.AssetsFileResolver(
                applicationContext.assets // use application context
            )
        )
        loadDefaultMonarchThemes()
        loadDefaultMonarchLanguages()
    }


    /**
     * Load default monarch themes
     *
     */
    private /*suspend*/ fun loadDefaultMonarchThemes() /*= withContext(Dispatchers.IO)*/ {
        val themes = arrayOf("darcula", "abyss", "quietlight", "solarized_dark")

        themes.forEach { name ->
            val path = "textmate/$name.json"
            io.github.rosemoe.sora.langs.monarch.registry.ThemeRegistry.loadTheme(
                io.github.rosemoe.sora.langs.monarch.registry.model.ThemeModel(
                    ThemeSource(path, name)
                ).apply {
                    if (name != "quietlight") {
                        isDark = true
                    }
                }, false
            )
        }

        io.github.rosemoe.sora.langs.monarch.registry.ThemeRegistry.setTheme("quietlight")
    }

    /**
     * Load default languages from Monarch
     */
    private fun loadDefaultMonarchLanguages() {
        MonarchGrammarRegistry.Companion.INSTANCE.loadGrammars(
            monarchLanguages {
                language("java") {
                    monarchLanguage = JavaLanguage
                    defaultScopeName()
                    languageConfiguration = "textmate/java/language-configuration.json"
                }
                language("kotlin") {
                    monarchLanguage = KotlinLanguage
                    defaultScopeName()
                    languageConfiguration = "textmate/kotlin/language-configuration.json"
                }
                language("python") {
                    monarchLanguage = PythonLanguage
                    defaultScopeName()
                    languageConfiguration = "textmate/python/language-configuration.json"
                }
                language("typescript") {
                    monarchLanguage = TypescriptLanguage
                    defaultScopeName()
                    languageConfiguration = "textmate/javascript/language-configuration.json"
                }
            }
        )
    }

    private fun loadDefaultLanguagesWithDSL() {
        GrammarRegistry.getInstance().loadGrammars(
            languages {
                language("java") {
                    grammar = "textmate/java/syntaxes/java.tmLanguage.json"
                    defaultScopeName()
                    languageConfiguration = "textmate/java/language-configuration.json"
                }
                language("kotlin") {
                    grammar = "textmate/kotlin/syntaxes/Kotlin.tmLanguage"
                    defaultScopeName()
                    languageConfiguration = "textmate/kotlin/language-configuration.json"
                }
                language("python") {
                    grammar = "textmate/python/syntaxes/python.tmLanguage.json"
                    defaultScopeName()
                    languageConfiguration = "textmate/python/language-configuration.json"
                }
            }
        )
    }



}