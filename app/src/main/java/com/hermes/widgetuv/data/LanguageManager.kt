package com.hermes.widgetuv.data

import android.content.Context
import java.util.Locale

object LanguageManager {
    private const val PREFS_NAME = "widget_uv_prefs"
    private const val KEY_LANG = "app_language" // "es", "en", or null (auto)

    fun isEnglish(context: Context): Boolean {
        val appContext = context.applicationContext ?: context
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_LANG, null)
        return if (saved != null) {
            saved == "en"
        } else {
            Locale.getDefault().language.equals("en", ignoreCase = true)
        }
    }

    fun setLanguage(context: Context, isEnglish: Boolean) {
        val appContext = context.applicationContext ?: context
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LANG, if (isEnglish) "en" else "es")
            .commit()
    }

    fun toggleLanguage(context: Context): Boolean {
        val current = isEnglish(context)
        val newLang = !current
        setLanguage(context, newLang)
        return newLang
    }
}
