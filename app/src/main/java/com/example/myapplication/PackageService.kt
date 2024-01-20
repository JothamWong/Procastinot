package com.example.myapplication

import kotlin.time.Duration

// jotham pls

interface PackageService {
    // null when package has no limit
    fun getLimit(p: Package): Duration?
}