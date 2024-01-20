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
    fun loadTimeLimit(packageName: Package): Duration? {
        val context = LocalContext.current
        val sharedPreferences = context.getSharedPreferences("AppTimeLimits", Context.MODE_PRIVATE)
        val timeString = sharedPreferences.getString(packageName.name, null) ?: return null

        val timeParts = timeString.split(":").map { it.toIntOrNull() ?: 0 }
        if (timeParts.size != 3) return null

        val hours = timeParts[0]
        val minutes = timeParts[1]
        val seconds = timeParts[2]

        val totalSeconds: Int = hours * 3600 + minutes * 60 + seconds
        return totalSeconds.toDuration(DurationUnit.SECONDS)
    }

}
