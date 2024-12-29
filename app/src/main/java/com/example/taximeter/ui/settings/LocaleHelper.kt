package com.example.taximeter.ui.settings

import android.content.Context
import java.util.Locale

class LocaleHelper {
    companion object {
        fun onAttach(context: Context): Context {
            val lang = getPersistedLanguage(context)
            return setLocale(context, lang)
        }

        fun setLocale(context: Context, languageCode: String): Context {
            persist(context, languageCode)

            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val configuration = context.resources.configuration
            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)

            return context.createConfigurationContext(configuration)
        }

        private fun persist(context: Context, language: String) {
            val preferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            preferences.edit().putString("language", language).apply()
        }

        private fun getPersistedLanguage(context: Context): String {
            val preferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            return preferences.getString("language", "en") ?: "en"
        }
    }
}
