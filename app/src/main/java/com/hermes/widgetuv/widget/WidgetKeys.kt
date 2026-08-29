package com.hermes.widgetuv.widget

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object WidgetKeys {
    val IS_ENGLISH = booleanPreferencesKey("is_english")
    val REFRESH_TRIGGER = stringPreferencesKey("refresh_trigger")
}
