package com.example.myapplication

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.pm.ResolveInfo
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import java.time.Duration
import java.time.Instant
import kotlin.time.toKotlinDuration
import kotlin.time.toJavaDuration

class MyAccessibilityService : AccessibilityService() {
    // in blacklist.kt, update blacklist, then call setter method here to update tracked packages
    // on initialization, blacklist.kt should expose a public fun to initialize the tracked packages
    private val TAG: String = "MyAccessibilityService"

    companion object {
        lateinit var instance: MyAccessibilityService
    }

    // TODO: Sync with persistent storage
    private val trackedPackages = hashMapOf<String, Duration>() // default test
    private val appForegroundTime = hashMapOf<String, Duration>()
    private val appTrackStart = hashMapOf<String, Instant>()
    private val appTrackEnd = hashMapOf<String, Instant>()
    private var isScrollLocked = hashMapOf<String, Boolean>()

    public fun updateTrackPackages(packageName: String, duration: Duration) {
        removeTrackPackages(packageName)
        addTrackPackages(packageName, duration)
    }
    public fun addTrackPackages(packageName: String, duration: Duration) {
        trackedPackages.set(packageName, duration)
    }
    public fun removeTrackPackages(packageName: String) {
        trackedPackages.remove(packageName)
    }

    public fun setScrollLock(packageName: String) {
        isScrollLocked[packageName] = true
    }
    override fun onServiceConnected() {
        val pm = this.applicationContext.packageManager
        super.onServiceConnected()
        instance = this
        val sharedPreferences = this.applicationContext.getSharedPreferences("AppTimeLimits", Context.MODE_PRIVATE)

        // Get all tracked packages

        val resolvedInfoList: List<ResolveInfo> = MainActivity.getAllApps(pm)
        for (resolveInfo in resolvedInfoList) {
            val appName: String = resolveInfo.activityInfo!!.packageName
            if (sharedPreferences.contains(appName)) {
                val durationString = sharedPreferences.getString(appName, "")
                val duration = kotlin.time.Duration.parseOrNull(durationString!!)!!.toJavaDuration()
                trackedPackages.set(appName, duration)
            }
        }


//        addTrackPackages("com.reddit.frontpage") // testing
        Log.d(TAG, "Service connected")
    }

//    fun parseKotlinDuration(kotlinDurationString: String): Duration {
//        val kotlinDuration = kotlin.time.Duration.ZERO
//        kotlinDuration
//    }

    private var lastUsedPackage = "deez nuts"
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
            val packageName : String = event?.run {
                windows.filter { it.isFocused }.singleOrNull()?.root?.packageName?.toString() ?: "unknown"
            } ?: "unknown"

            when(event?.eventType) {
                AccessibilityEvent.TYPE_WINDOWS_CHANGED -> handleWindowChangeEvent(packageName)
                AccessibilityEvent.TYPE_VIEW_SCROLLED -> handleScrollEvent(event, packageName)
            }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun handleWindowChangeEvent(packageName: String) {
        // Implementation of window change handling using packageName
        if(packageName != "null" && packageName != lastUsedPackage) {
            Log.d(TAG, "window to da balls changed")
            Log.d(TAG, "WINDOW: $packageName")

            val currentTime = Instant.now()

            // A (lasUsedPackage) -switch-> B (packageName)

            // EXIT ROUTINE FOR A
            if (trackedPackages.contains(lastUsedPackage)) {
                // Update exit time of lastUsedPackage if lastUsedPackage is tracked
                appTrackEnd[lastUsedPackage] = currentTime
                // Update total time spent
                val timeSpent = Duration.between(appTrackStart[lastUsedPackage], currentTime)
//                val timeSpent = currentTime.minusMillis(appTrackStart[lastUsedPackage]!!.toEpochMilli())
//                val _timeSpent = currentTime.minus(appTrackStart[lastUsedPackage])
                val prevTimeSpent = appForegroundTime.getOrDefault(lastUsedPackage, Duration.ZERO)
                appForegroundTime[lastUsedPackage] = prevTimeSpent.plus(timeSpent)
            }

            // ENTRY ROUTINE FOR B
            if (trackedPackages.contains(packageName)) {
                Log.d(TAG, "ok we in timey wimey bizniz")
                // Init package time
                // Update entry time of packageName if packageName is tracked
                if (!appTrackStart.containsKey(packageName)) {
                    appTrackStart[packageName] = currentTime
                    appForegroundTime[packageName] = Duration.ZERO
                    Log.d(TAG, "wow tracking virgin, popping app cherry")
                    Log.d(TAG, "$packageName's new track time starts at $currentTime")
                } else {
                    // Check if it has been 24 hours since app was being tracked
                    val lastStartedTracking = appTrackStart[packageName]!!
                    if (Duration.between(lastStartedTracking, currentTime).toHours() > 24) {
                        appTrackStart[packageName] = currentTime
                        appForegroundTime[packageName] = Duration.ZERO
                        Log.d(TAG, "w0w new day new app")
                        Log.d(TAG, "$packageName's new track time starts at $currentTime")
                    }
                }
            }

            val limit = loadTimeLimit(applicationContext, packageName)
            if (limit != null) {
                OverlayService.instance?.setDuration(limit.minus(appForegroundTime[packageName]!!.toKotlinDuration()))
            } else {
                OverlayService.instance?.setDuration(null)
            }

            // Update lastUsedPackage
            lastUsedPackage = packageName
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun handleScrollEvent(event: AccessibilityEvent, packageName: String) {
        // Implementation of scroll event handling using packageName
        if (trackedPackages.contains(packageName)) {
            val currentTime = Instant.now()
            val appStartTime = appTrackStart[packageName]!!
            val timeSpent = Duration.between(appStartTime, currentTime)
            val totalTime = appForegroundTime.getOrDefault(packageName, Duration.ZERO).plus(timeSpent)
            appForegroundTime[packageName] = totalTime

            // Log usage values
            Log.d(TAG, "Scroll Event: Time you last entered this app=$appStartTime")
            Log.d(TAG, "Time spent scrolling since last app entry=$timeSpent")
            Log.d(TAG, "Total app usage=$totalTime")

            // Check if it has been 24 hours since app was being tracked
            val lastStartedTracking = appTrackStart[packageName]!!
            if (Duration.between(lastStartedTracking, currentTime).toHours() > 24) {
                appTrackStart[packageName] = currentTime
                appForegroundTime[packageName] = Duration.ZERO
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
