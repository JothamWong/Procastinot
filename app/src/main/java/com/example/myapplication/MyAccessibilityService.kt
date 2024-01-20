package com.example.myapplication

import android.accessibilityservice.AccessibilityService
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import java.util.Calendar

class MyAccessibilityService : AccessibilityService() {
    private var scrollCoefficient = 1.0


    override fun onServiceConnected() {
        super.onServiceConnected()
        println("Service connected")
    }


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        println(event)

        if (event?.eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            println("Shits in focus")
            val packageName = event.packageName.toString()
            if (trackedPackages.contains(packageName)) {
                val currentTime = System.currentTimeMillis()

                // Init package
                if (!appForegroundTime.containsKey(packageName)) {
                    appForegroundTime[packageName] = currentTime
                } else {
                    // Increment the foreground time
                    val startTime = appForegroundTime[packageName] ?: currentTime
                    val timeSpent = currentTime - startTime
                    appForegroundTime[packageName] = timeSpent
                }
            }
        }
        if (event?.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            val scrollY = event.scrollDeltaY

            val timeSpent = getTotalTimeInForeground(event.packageName.toString())
            // For now, we'll just log the scroll values
            println("Scroll Event: Y=$scrollY, Time Spent=$timeSpent")
        }
    }

    private val trackedPackages = setOf("com.reddit.frontpage")
    private val appForegroundTime = hashMapOf<String, Long>()

    private fun getTotalTimeInForeground(packageName: String): Long {
        return appForegroundTime[packageName] ?: 0L
    }

    override fun onInterrupt() {
        // Handle interruptions
    }
}
