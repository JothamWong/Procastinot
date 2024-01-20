package com.example.myapplication

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi

class MyAccessibilityService : AccessibilityService() {
    // in blacklist.kt, update blacklist, then call setter method here to update tracked packages
    // on initialization, blacklist.kt should expose a public fun to initialize the tracked packages

    private val trackedPackages = mutableSetOf<String>("com.reddit.frontpage")
    private val appForegroundTime = hashMapOf<String, Long>()
    private val appTrackStart = hashMapOf<String, Long>()
    private val appTrackEnd = hashMapOf<String, Long>()

    public fun addTrackPackages(packageName: String) {
        trackedPackages.add(packageName)
    }
    public fun removeTrackPages(packageName: String) {
        trackedPackages.remove(packageName)
    }

    public fun getAppForegroundTime(packageName: String): Long {
        return appForegroundTime.getOrDefault(packageName, -1)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        addTrackPackages("com.reddit.frontpage") // testing
        println("Service connected")
    }

    private var lastUsedPackage = "deez nuts"
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event?.eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            try {
                val packageName = windows.filter { it.isFocused }.singleOrNull()?.root?.packageName.toString()
                if(packageName != "null" && packageName != lastUsedPackage) {
                    println("window to da balls changed")
                    println("WINDOW: $packageName")

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
                        println("ok we in timey wimey bizniz")
                        // Init package time
                        // Update entry time of packageName if packageName is tracked
                        if (!appTrackStart.containsKey(packageName)) {
                            appTrackStart[packageName] = currentTime
                            appForegroundTime[packageName] = 0
                            println("wow tracking virgin, popping app cherry")
                            println("$packageName's new track time starts at $currentTime")
                        } else {
                            // Check if it has been 24 hours since app was being tracked
                            val lastStartedTracking = appTrackStart[packageName]!!
                            if (currentTime - lastStartedTracking > 1000 * 60 * 60 * 24) {
                                appTrackStart[packageName] = currentTime
                                appForegroundTime[packageName] = 0
                                println("w0w new day new app")
                                println("$packageName's new track time starts at $currentTime")
                            }
                        }
                    }

                    // Update lastUsedPackage
                    lastUsedPackage = packageName
                }

            } catch (e: NullPointerException) {
                println("nope u fucked up")
            }
        }
        if (event?.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            val packageName = event.packageName.toString()
            if(trackedPackages.contains(packageName)) {
                val currentTime = System.currentTimeMillis()
                val appStartTime = appTrackStart[packageName]!!
                val timeSpent = currentTime - appStartTime
                val totalTime = appForegroundTime[packageName]!! + timeSpent
                appForegroundTime[packageName] = totalTime

                // Log usage values
                println("Scroll Event: Time you last entered this app=$appStartTime")
                println("Time spent scrolling since last app entry=$timeSpent")
                println("Total app usage=$totalTime")

                // Check if it has been 24 hours since app was being tracked
                if(totalTime > 1000 * 60 * 60 * 24) {
                    appTrackStart[packageName] = currentTime
                    appForegroundTime[packageName] = 0
                    println("w0w new day new app")
                    println("$packageName's new track time starts at $currentTime")
                }
            }
        }
    }


    override fun onInterrupt() {
        // Handle interruptions
    }
}
