package com.example.taximeter.ui.history

data class HistoryItem(
    val dateTime: String,
    val startLocation: String,
    val endLocation: String,
    val fare: String,
    val distance: String
)
