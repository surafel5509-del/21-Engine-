package com.example.engine.debug

import com.example.engine.utils.Logger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel {
    INFO,
    WARNING,
    ERROR
}

data class ConsoleMessage(
    val id: String,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val timestamp: String = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
)

/**
 * Central engine log buffer for capturing and streaming warnings, print statements, and runtime errors to the UI Console Dock.
 */
object DebugConsoleLog {
    private val logs = mutableListOf<ConsoleMessage>()
    private val listeners = mutableListOf<(List<ConsoleMessage>) -> Unit>()

    @Synchronized
    fun log(level: LogLevel, tag: String, message: String) {
        val entry = ConsoleMessage(
            id = System.nanoTime().toString(),
            level = level,
            tag = tag,
            message = message
        )
        logs.add(entry)
        if (logs.size > 200) {
            logs.removeAt(0)
        }
        notifyListeners()

        when (level) {
            LogLevel.INFO -> Logger.i(tag, message)
            LogLevel.WARNING -> Logger.w(tag, message)
            LogLevel.ERROR -> Logger.e(tag, message)
        }
    }

    fun info(tag: String, message: String) = log(LogLevel.INFO, tag, message)
    fun warning(tag: String, message: String) = log(LogLevel.WARNING, tag, message)
    fun error(tag: String, message: String) = log(LogLevel.ERROR, tag, message)

    @Synchronized
    fun getLogs(): List<ConsoleMessage> = logs.toList()

    @Synchronized
    fun clear() {
        logs.clear()
        notifyListeners()
    }

    fun addListener(listener: (List<ConsoleMessage>) -> Unit) {
        listeners.add(listener)
        listener(getLogs())
    }

    fun removeListener(listener: (List<ConsoleMessage>) -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        val current = getLogs()
        listeners.forEach { it(current) }
    }
}
