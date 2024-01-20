package com.example.myapplication

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi

class MyAccessibilityService : AccessibilityService() {
    // in blacklist.kt, update blacklist, then call setter method here to update tracked packages
    // on initialization, blacklist.kt should expose a public fun to initialize the tracked packages
    private val TAG: String = "MyAccessibilityService"

    // TODO: Sync with persistent storage
    private val trackedPackages = mutableSetOf<String>("com.reddit.frontpage") // default test
    private val appForegroundTime = hashMapOf<String, Long>()
    private val appTrackStart = hashMapOf<String, Long>()
    private val appTrackEnd = hashMapOf<String, Long>()
    private var isScrollLocked = hashMapOf<String, Boolean>()

    public fun addTrackPackages(packageName: String) {
        trackedPackages.add(packageName)
    }
    public fun removeTrackPages(packageName: String) {
        trackedPackages.remove(packageName)
    }

    public fun getAppForegroundTime(packageName: String): Long {
        return appForegroundTime.getOrDefault(packageName, -1)
    }

    public fun setScrollLock(packageName: String) {
        isScrollLocked[packageName] = true
    }
    override fun onServiceConnected() {
        super.onServiceConnected()
        addTrackPackages("com.reddit.frontpage") // testing
        Log.d(TAG, "Service connected")
    }

    private var lastUsedPackage = "deez nuts"
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            val packageName : String = event?.run {
                windows.filter { it.isFocused }.singleOrNull()?.root?.packageName?.toString() ?: "unknown"
            } ?: "unknown"

            when(event?.eventType) {
                AccessibilityEvent.TYPE_WINDOWS_CHANGED -> handleWindowChangeEvent(packageName)
                AccessibilityEvent.TYPE_VIEW_SCROLLED -> handleScrollEvent(event, packageName)
            }
        } catch (e: NullPointerException) {
            Log.e(TAG, "nope u fucked up")
        }
    }

    private fun handleWindowChangeEvent(packageName: String) {
        // Implementation of window change handling using packageName
        if(packageName != "null" && packageName != lastUsedPackage) {
            Log.d(TAG, "window to da balls changed")
            Log.d(TAG, "WINDOW: $packageName")

            val currentTime = System.currentTimeMillis()

            // A (lasUsedPackage) -switch-> B (packageName)

            // EXIT ROUTINE FOR A
            if(trackedPackages.contains(lastUsedPackage)) {
                // Update exit time of lastUsedPackage if lastUsedPackage is tracked
                appTrackEnd[lastUsedPackage] = currentTime
                // Update total time spent
                val timeSpent = currentTime - appTrackStart[lastUsedPackage]!! // guaranteed because of dummy start package
                val prevTimeSpent = appForegroundTime[lastUsedPackage]!!
                appForegroundTime[lastUsedPackage] = prevTimeSpent + timeSpent
            }

            // ENTRY ROUTINE FOR B
            if (trackedPackages.contains(packageName)) {
                Log.d(TAG, "ok we in timey wimey bizniz")
                // Init package time
                // Update entry time of packageName if packageName is tracked
                if (!appTrackStart.containsKey(packageName)) {
                    appTrackStart[packageName] = currentTime
                    appForegroundTime[packageName] = 0
                    Log.d(TAG, "wow tracking virgin, popping app cherry")
                    Log.d(TAG, "$packageName's new track time starts at $currentTime")
                } else {
                    // Check if it has been 24 hours since app was being tracked
                    val lastStartedTracking = appTrackStart[packageName]!!
                    if (currentTime - lastStartedTracking > 1000 * 60 * 60 * 24) {
                        appTrackStart[packageName] = currentTime
                        appForegroundTime[packageName] = 0
                        Log.d(TAG, "w0w new day new app")
                        Log.d(TAG, "$packageName's new track time starts at $currentTime")
                    }
                }
            }

            // Update lastUsedPackage
            lastUsedPackage = packageName
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun handleScrollEvent(event: AccessibilityEvent, packageName: String) {
        // Implementation of scroll event handling using packageName
        if(trackedPackages.contains(packageName)) {
            val currentTime = System.currentTimeMillis()
            val appStartTime = appTrackStart[packageName]!!
            val timeSpent = currentTime - appStartTime
            val totalTime = appForegroundTime[packageName]!! + timeSpent
            appForegroundTime[packageName] = totalTime

            // Log usage values
            Log.d(TAG, "Scroll Event: Time you last entered this app=$appStartTime")
            Log.d(TAG, "Time spent scrolling since last app entry=$timeSpent")
            Log.d(TAG, "Total app usage=$totalTime")

            // Check if it has been 24 hours since app was being tracked
            if(totalTime > 1000 * 60 * 60 * 24) {
                appTrackStart[packageName] = currentTime
                appForegroundTime[packageName] = 0
                // TODO: Reset scroll lock for package
                // isScrollLocked[packageName] = false
                Log.d(TAG, "w0w new day new app")
                Log.d(TAG, "$packageName's new track time starts at $currentTime")
            }
        }

        // TODO: Implement check whether app limit has exceeded and set scroll lock

        val scrollX = event.scrollDeltaX
        val scrollY = event.scrollDeltaY
        if (isScrollLocked.getOrDefault(packageName, false)){
            Log.d(TAG, "scrolling backwards")
            event.source?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
        }

        // For now, we'll just log the scroll values
        Log.d(TAG, "Scroll Event: X=$scrollX, Y=$scrollY")

    }

    override fun onInterrupt() {
        // Handle interruptions
    }
}
