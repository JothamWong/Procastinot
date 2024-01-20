package com.example.myapplication

import android.accessibilityservice.AccessibilityGestureEvent
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Build
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi

class MyAccessibilityService : AccessibilityService() {
    private var scrollCoefficient = 1.0


    override fun onServiceConnected() {
        super.onServiceConnected()
        println("Service connected")
    }


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        println(event)
        if (event?.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            val scrollX = event.scrollDeltaX
            val scrollY = event.scrollDeltaY
            println(scrollY)
//            if (scrollY >= 0){
//                println("scrolling backwards")
//                event.source?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
//            }

            // For now, we'll just log the scroll values
            println("Scroll Event: X=$scrollX, Y=$scrollY")
        }
    }

    override fun onInterrupt() {
        // Handle interruptions
    }
}
