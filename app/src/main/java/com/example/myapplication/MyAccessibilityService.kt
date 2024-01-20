package com.example.myapplication

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi

class MyAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        println("Service connected")
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        println(event)
        if (event?.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            val scrollX = event.scrollDeltaX
            val scrollY = event.scrollDeltaY
            // For now, we'll just log the scroll values
            println("Scroll Event: X=$scrollX, Y=$scrollY")
        }
    }

    override fun onInterrupt() {
        // Handle interruptions
    }
}
