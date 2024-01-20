package com.example.myapplication

// amit pls

interface UsageService {
    fun usageStart(p: Package)
    fun usageEnd(p: Package)
    fun isLimitExceeded(p: Package): Boolean
}