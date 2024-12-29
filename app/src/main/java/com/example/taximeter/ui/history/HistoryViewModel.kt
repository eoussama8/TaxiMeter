package com.example.taximeter.ui.history

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HistoryViewModel : ViewModel() {

    private val _historyList = MutableLiveData<List<HistoryItem>>(emptyList())
    val historyList: LiveData<List<HistoryItem>> get() = _historyList

    fun addHistoryItem(item: HistoryItem) {
        val currentList = _historyList.value ?: emptyList()
        _historyList.value = currentList + item
    }
}
