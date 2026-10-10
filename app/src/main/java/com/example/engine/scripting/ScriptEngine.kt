package com.example.engine.scripting

import com.example.engine.math.Vector2
import com.example.studio.model.GameNode
import com.example.studio.model.GameProject

data class ScriptError(
    val lineNumber: Int,
    val message: String,
    val sourceLine: String = ""
)

class ScriptEnvironment(
    val node: GameNode,
    val project: GameProject,
    val onPlaySound: ((String) -> Unit)? = null,
    val onSpawnPrefab: ((String, Float, Float) -> Unit)? = null,
    val onDestroyNode: ((String) -> Unit)? = null,
    val onLogMessage: ((String) -> Unit)? = null
) {
    val variables = mutableMapOf<String, Any>(
        "position_x" to node.posX,
        "position_y" to node.posY,
        "scale_x" to node.scaleX,
        "scale_y" to node.scaleY,
        "rotation" to node.rotation,
        "move_speed" to node.moveSpeed,
        "jump_force" to node.jumpForce,
        "health" to node.health,
        "max_health" to node.maxHealth,
        "score_value" to node.scoreValue,
        "is_visible" to node.isVisible
    )

    fun syncFromNode() {
        variables["position_x"] = node.posX
        variables["position_y"] = node.posY
        variables["scale_x"] = node.scaleX
        variables["scale_y"] = node.scaleY
        variables["rotation"] = node.rotation
        variables["move_speed"] = node.moveSpeed
        variables["jump_force"] = node.jumpForce
        variables["health"] = node.health
        variables["max_health"] = node.maxHealth
        variables["score_value"] = node.scoreValue
        variables["is_visible"] = node.isVisible
    }

    fun syncToNode() {
        node.posX = (variables["position_x"] as? Number)?.toFloat() ?: node.posX
        node.posY = (variables["position_y"] as? Number)?.toFloat() ?: node.posY
        node.scaleX = (variables["scale_x"] as? Number)?.toFloat() ?: node.scaleX
        node.scaleY = (variables["scale_y"] as? Number)?.toFloat() ?: node.scaleY
        node.rotation = (variables["rotation"] as? Number)?.toFloat() ?: node.rotation
        node.moveSpeed = (variables["move_speed"] as? Number)?.toFloat() ?: node.moveSpeed
        node.jumpForce = (variables["jump_force"] as? Number)?.toFloat() ?: node.jumpForce
        node.health = (variables["health"] as? Number)?.toInt() ?: node.health
        node.maxHealth = (variables["max_health"] as? Number)?.toInt() ?: node.maxHealth
        node.scoreValue = (variables["score_value"] as? Number)?.toInt() ?: node.scoreValue
        node.isVisible = (variables["is_visible"] as? Boolean) ?: node.isVisible
    }
}

object ScriptEngine {

    private val errors = mutableListOf<ScriptError>()
    fun getErrors(): List<ScriptError> = errors.toList()
    fun clearErrors() { errors.clear() }

    fun parseAndValidate(scriptCode: String): List<ScriptError> {
        val errs = mutableListOf<ScriptError>()
        val lines = scriptCode.lines()
        for ((index, rawLine) in lines.withIndex()) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue

            if (line.startsWith("func ") && !line.endsWith(":")) {
                errs.add(ScriptError(index + 1, "Expected ':' at end of function declaration", line))
            }
        }
        return errs
    }

    fun executeHook(
        hookName: String,
        scriptCode: String,
        env: ScriptEnvironment,
        deltaTime: Float = 0.016f
    ) {
        if (scriptCode.isBlank()) return
        env.syncFromNode()

        val lines = scriptCode.lines()
        var insideTargetHook = false
        var hookIndent = 0

        for ((lineIdx, rawLine) in lines.withIndex()) {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) continue

            if (trimmed.startsWith("func ")) {
                val funcName = trimmed.removePrefix("func ").substringBefore("(").trim()
                if (funcName == hookName) {
                    insideTargetHook = true
                    hookIndent = rawLine.indexOfFirst { !it.isWhitespace() }
                    continue
                } else if (insideTargetHook) {
                    // Reached next function
                    break
                }
            }

            if (insideTargetHook) {
                val currentIndent = rawLine.indexOfFirst { !it.isWhitespace() }
                if (currentIndent <= hookIndent && trimmed.startsWith("func ")) {
                    break
                }

                try {
                    executeLine(trimmed, env, deltaTime)
                } catch (e: Exception) {
                    val err = ScriptError(lineIdx + 1, e.message ?: "Script execution error", trimmed)
                    errors.add(err)
                    env.onLogMessage?.invoke("[ScriptError L${lineIdx + 1}] ${err.message}")
                }
            }
        }

        env.syncToNode()
    }

    private fun executeLine(line: String, env: ScriptEnvironment, delta: Float) {
        val vars = env.variables

        // 1. var assignment: var name = value
        if (line.startsWith("var ")) {
            val expr = line.removePrefix("var ").trim()
            val parts = expr.split("=", limit = 2)
            if (parts.size == 2) {
                val varName = parts[0].trim()
                val valStr = parts[1].trim()
                vars[varName] = evaluateExpression(valStr, vars, delta)
            }
            return
        }

        // 2. Direct property/variable mutation e.g. position_x += 100 * delta
        if (line.contains("+=") || line.contains("-=") || line.contains("*=") || line.contains("/=") || line.contains("=")) {
            val op = when {
                line.contains("+=") -> "+="
                line.contains("-=") -> "-="
                line.contains("*=") -> "*="
                line.contains("/=") -> "/="
                else -> "="
            }
            val parts = line.split(op, limit = 2)
            if (parts.size == 2) {
                val varName = parts[0].trim()
                val rhsStr = parts[1].trim()
                val rhsVal = evaluateExpression(rhsStr, vars, delta)
                val currentVal = (vars[varName] as? Number)?.toDouble() ?: 0.0

                val finalVal = when (op) {
                    "+=" -> currentVal + ((rhsVal as? Number)?.toDouble() ?: 0.0)
                    "-=" -> currentVal - ((rhsVal as? Number)?.toDouble() ?: 0.0)
                    "*=" -> currentVal * ((rhsVal as? Number)?.toDouble() ?: 1.0)
                    "/=" -> currentVal / ((rhsVal as? Number)?.toDouble() ?: 1.0)
                    else -> (rhsVal as? Number)?.toDouble() ?: 0.0
                }
                vars[varName] = finalVal.toFloat()
                return
            }
        }

        // 3. Function Calls
        if (line.contains("(") && line.endsWith(")")) {
            val funcName = line.substringBefore("(").trim()
            val argStr = line.substringAfter("(").substringBeforeLast(")").trim()
            val args = if (argStr.isEmpty()) emptyList() else argStr.split(",").map { it.trim() }

            when (funcName) {
                "print" -> {
                    val msg = args.joinToString(" ") { evaluateExpression(it, vars, delta).toString() }
                    env.onLogMessage?.invoke("[Script] $msg")
                }
                "play_sound" -> {
                    val soundKey = args.firstOrNull()?.replace("\"", "")?.replace("'", "") ?: "jump"
                    env.onPlaySound?.invoke(soundKey)
                }
                "spawn_prefab" -> {
                    val prefab = args.getOrNull(0)?.replace("\"", "")?.replace("'", "") ?: "bullet"
                    val x = (args.getOrNull(1)?.let { (evaluateExpression(it, vars, delta) as? Number)?.toFloat() }) ?: env.node.posX
                    val y = (args.getOrNull(2)?.let { (evaluateExpression(it, vars, delta) as? Number)?.toFloat() }) ?: env.node.posY
                    env.onSpawnPrefab?.invoke(prefab, x, y)
                }
                "destroy" -> {
                    env.onDestroyNode?.invoke(env.node.id)
                }
            }
        }
    }

    private fun evaluateExpression(expr: String, vars: Map<String, Any>, delta: Float): Any {
        val clean = expr.trim()
        if (clean == "delta") return delta
        if (clean == "true") return true
        if (clean == "false") return false

        if (clean.startsWith("\"") && clean.endsWith("\"")) {
            return clean.substring(1, clean.length - 1)
        }

        clean.toDoubleOrNull()?.let { return it.toFloat() }

        if (vars.containsKey(clean)) {
            return vars[clean]!!
        }

        // Simple multiplication: a * delta
        if (clean.contains("*")) {
            val parts = clean.split("*")
            var product = 1.0
            for (p in parts) {
                val valP = evaluateExpression(p.trim(), vars, delta)
                val numP = (valP as? Number)?.toDouble() ?: 1.0
                product *= numP
            }
            return product.toFloat()
        }

        // Simple addition: a + b
        if (clean.contains("+")) {
            val parts = clean.split("+")
            var sum = 0.0
            for (p in parts) {
                val valP = evaluateExpression(p.trim(), vars, delta)
                val numP = (valP as? Number)?.toDouble() ?: 0.0
                sum += numP
            }
            return sum.toFloat()
        }

        return clean
    }
}
