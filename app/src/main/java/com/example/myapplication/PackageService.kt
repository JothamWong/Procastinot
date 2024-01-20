package com.example.myapplication

import kotlin.time.Duration

// jotham pls

interface PackageService {
    fun getLimit(p: Package): Duration
}