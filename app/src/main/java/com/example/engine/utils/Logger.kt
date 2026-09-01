package com.example.engine.utils

import android.util.Log

/**
 * Engine logger with tags and level filtering.
 */
object Logger {
    enum class Level { VERBOSE, DEBUG, INFO, WARN, ERROR, NONE }

    var minLevel: Level = Level.DEBUG
    var tagPrefix: String = "GameEngine"

    fun v(tag: String, msg: String) {
        if (minLevel <= Level.VERBOSE) Log.v("$tagPrefix:$tag", msg)
    }

    fun d(tag: String, msg: String) {
        if (minLevel <= Level.DEBUG) Log.d("$tagPrefix:$tag", msg)
    }

    fun i(tag: String, msg: String) {
        if (minLevel <= Level.INFO) Log.i("$tagPrefix:$tag", msg)
    }

    fun w(tag: String, msg: String) {
        if (minLevel <= Level.WARN) Log.w("$tagPrefix:$tag", msg)
    }

    fun e(tag: String, msg: String, tr: Throwable? = null) {
        if (minLevel <= Level.ERROR) {
            if (tr != null) Log.e("$tagPrefix:$tag", msg, tr) else Log.e("$tagPrefix:$tag", msg)
        }
    }
}
