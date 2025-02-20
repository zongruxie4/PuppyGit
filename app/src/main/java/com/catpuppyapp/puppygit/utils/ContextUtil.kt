package com.catpuppyapp.puppygit.utils

import android.content.Context
import java.util.Locale

object ContextUtil {
    private const val TAG = "ContextUtil"

    fun getLocalizedContext(newBase: Context):Context {
        try {
            // check language
            val languageCode = LanguageUtil.getLangCode(newBase)
            if(!LanguageUtil.isSupportedLanguage(languageCode)) {
                // here should run faster as possible, throw Exception is bad for running speed
//               // throw RuntimeException("found unsupported lang in config, will try auto detect language")

                // auto detected or unsupported language
                return newBase
            }


            // found supported language
            return createContextByLanguageCode(languageCode, newBase)
        }catch (e:Exception) {
            MyLog.e(TAG, "#getLocalizedContext err: ${e.localizedMessage}")

            // auto detected or unsupported language
            return newBase
        }

    }

    private fun createContextByLanguageCode(languageCode: String, baseContext: Context): Context {
        // split language codes, e.g. split "zh-rCN" to "zh" and "CN"
        val (language, country) = LanguageUtil.splitLanguageCode(languageCode)
        val locale = if (country.isBlank()) Locale(language) else Locale(language, country)
        Locale.setDefault(locale)
        val config = baseContext.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return baseContext.createConfigurationContext(config)
    }
}
