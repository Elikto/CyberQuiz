package com.example.cyberquiz

import android.app.Application

/**
 * Application process entry point.
 *
 * Update discovery is intentionally handled by the UI as a silent, passive check.
 * No dialog, toast, installer flow, or Android settings screen is ever launched from
 * the application lifecycle.
 */
class CyberQuizApplication : Application()
