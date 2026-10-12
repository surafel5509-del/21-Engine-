package com.example.engine.scripting

import com.example.engine.scene.GameObject
import com.example.engine.utils.Logger

/**
 * Lightweight runtime interpreter supporting GDScript / Lua style syntax.
 * Evaluates variables, basic arithmetic, position modifications, and lifecycle hooks (_ready, _process).
 */
class ScriptInterpreter(
    val scriptCode: String,
    val gameObject: GameObject? = null
) {
    private val variables = mutableMapOf<String, Any>()

    init {
        parseInitialVariables()
    }

    private fun parseInitialVariables() {
        val lines = scriptCode.lines()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("var ")) {
                val statement = trimmed.removePrefix("var ").trim()
                if (statement.contains("=")) {
                    val parts = statement.split("=", limit = 2)
                    val varName = parts[0].trim()
                    val rawVal = parts[1].trim()
                    val parsedVal: Any = when {
                        rawVal.toFloatOrNull() != null -> rawVal.toFloat()
                        rawVal.equals("true", ignoreCase = true) -> true
                        rawVal.equals("false", ignoreCase = true) -> false
                        else -> rawVal.removeSurrounding("\"", "\"")
                    }
                    variables[varName] = parsedVal
                }
            }
        }
    }

    fun executeLifecycleHook(hookName: String, deltaTime: Float = 0f) {
        if (!scriptCode.contains(hookName)) return

        val lines = scriptCode.lines()
        var insideHook = false

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("func $hookName") || trimmed.startsWith("def $hookName")) {
                insideHook = true
                continue
            }

            if (insideHook) {
                if (trimmed.startsWith("func ") || trimmed.startsWith("def ")) {
                    break // end of block
                }
                if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                    executeStatement(trimmed, deltaTime)
                }
            }
        }
    }

    private fun executeStatement(statement: String, deltaTime: Float) {
        try {
            // Position movement: position.x += 10 * delta
            if (statement.startsWith("position.x")) {
                val value = parseRightHandSide(statement.substringAfter("="), deltaTime)
                gameObject?.transform?.position?.let {
                    if (statement.contains("+=")) it.x += value
                    else if (statement.contains("-=")) it.x -= value
                    else if (statement.contains("=")) it.x = value
                }
            } else if (statement.startsWith("position.y")) {
                val value = parseRightHandSide(statement.substringAfter("="), deltaTime)
                gameObject?.transform?.position?.let {
                    if (statement.contains("+=")) it.y += value
                    else if (statement.contains("-=")) it.y -= value
                    else if (statement.contains("=")) it.y = value
                }
            } else if (statement.startsWith("rotation")) {
                val value = parseRightHandSide(statement.substringAfter("="), deltaTime)
                gameObject?.transform?.let {
                    if (statement.contains("+=")) it.rotation += value
                    else if (statement.contains("-=")) it.rotation -= value
                    else if (statement.contains("=")) it.rotation = value
                }
            } else if (statement.startsWith("print(")) {
                val content = statement.substringAfter("print(").substringBeforeLast(")").trim()
                val evaluated = variables[content] ?: content.removeSurrounding("\"", "\"")
                Logger.d("ScriptInterpreter", "PRINT: $evaluated")
            }
        } catch (e: Exception) {
            Logger.e("ScriptInterpreter", "Error executing statement: $statement", e)
        }
    }

    private fun parseRightHandSide(rhs: String, deltaTime: Float): Float {
        var expr = rhs.trim().replace("delta", deltaTime.toString())
        for ((k, v) in variables) {
            if (v is Number) {
                expr = expr.replace(k, v.toString())
            }
        }

        // Simple multiplication support e.g. "100 * 0.016"
        if (expr.contains("*")) {
            val factors = expr.split("*")
            return factors.fold(1.0f) { acc, factor -> acc * (factor.trim().toFloatOrNull() ?: 1.0f) }
        }

        return expr.toFloatOrNull() ?: 0f
    }

    fun getVariable(name: String): Any? = variables[name]
    fun setVariable(name: String, value: Any) { variables[name] = value }
}
