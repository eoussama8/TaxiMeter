package com.example.taximeter.ui.settings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {
    private val _language = MutableLiveData<String>()
    val language: LiveData<String> = _language

    fun setLanguage(languageCode: String) {
        _language.value = languageCode
    }
}