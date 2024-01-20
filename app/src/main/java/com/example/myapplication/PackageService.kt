package com.example.myapplication

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

// jotham pls

interface PackageService {
    // null when package has no limit
    @Composable
    fun getLimit(p: Package): Duration? {
        val context = LocalContext.current
        val sharedPreferences = context.getSharedPreferences("AppTimeLimits", Context.MODE_PRIVATE)
        val timeString = sharedPreferences.getString(p.name, null) ?: return null

        val timeParts = timeString.split(":").map { it.toIntOrNull() ?: 0 }
        if (timeParts.size != 2) return null

        val hours = timeParts[0]
        val minutes = timeParts[1]

        val totalMinutes: Int = hours * 60 + minutes
        return totalMinutes.toDuration(DurationUnit.MINUTES)
    }
}
