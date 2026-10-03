package com.example.engine.scripting

import com.example.engine.utils.Logger
import com.example.studio.model.GameNode

/**
 * Lightweight GDScript/Lua-like script execution engine for runtime nodes.
 * Supports script variables, function hooks (_ready, _process), basic math, and node transform updates.
 */
class GdScriptEngine(
    val node: GameNode,
    val scriptCode: String
) {
    val variables: MutableMap<String, Any> = mutableMapOf()
    private val readyStatements: MutableList<String> = mutableListOf()
    private val processStatements: MutableList<String> = mutableListOf()

    init {
        parseScript()
    }

    private fun parseScript() {
        var currentFunction: String? = null
        val lines = scriptCode.lines()

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#")) continue

            if (line.startsWith("var ")) {
                parseVariable(line)
            } else if (line.startsWith("func _ready():")) {
                currentFunction = "_ready"
            } else if (line.startsWith("func _process(")) {
                currentFunction = "_process"
            } else if (line.startsWith("func ")) {
                currentFunction = line.substringAfter("func ").substringBefore("(").trim()
            } else if (currentFunction != null) {
                when (currentFunction) {
                    "_ready" -> readyStatements.add(line)
                    "_process" -> processStatements.add(line)
                }
            }
        }
    }

    private fun parseVariable(line: String) {
        val content = line.removePrefix("var ").trim()
        val parts = content.split("=")
        if (parts.size == 2) {
            val varName = parts[0].trim()
            val valStr = parts[1].trim()

            val numValue = valStr.toFloatOrNull()
            if (numValue != null) {
                variables[varName] = numValue
            } else if (valStr.equals("true", ignoreCase = true) || valStr.equals("false", ignoreCase = true)) {
                variables[varName] = valStr.toBoolean()
            } else {
                variables[varName] = valStr.replace("\"", "")
            }
        }
    }

    fun executeReady() {
        for (statement in readyStatements) {
            executeStatement(statement, 0f)
        }
    }

    fun executeProcess(delta: Float) {
        for (statement in processStatements) {
            executeStatement(statement, delta)
        }
    }

    private fun executeStatement(statement: String, delta: Float) {
        try {
            if (statement.startsWith("print(")) {
                val msg = statement.substringAfter("print(").substringBeforeLast(")")
                Logger.d("GDScript[${node.name}]: $msg")
            } else if (statement.contains("position.x") || statement.contains("pos_x") || statement.contains("posX")) {
                if (statement.contains("+=")) {
                    val step = evaluateExpression(statement.substringAfter("+="), delta)
                    node.posX += step
                } else if (statement.contains("-=")) {
                    val step = evaluateExpression(statement.substringAfter("-="), delta)
                    node.posX -= step
                }
            } else if (statement.contains("position.y") || statement.contains("pos_y") || statement.contains("posY")) {
                if (statement.contains("+=")) {
                    val step = evaluateExpression(statement.substringAfter("+="), delta)
                    node.posY += step
                } else if (statement.contains("-=")) {
                    val step = evaluateExpression(statement.substringAfter("-="), delta)
                    node.posY -= step
                }
            } else if (statement.contains("rotation") || statement.contains("rot")) {
                if (statement.contains("+=")) {
                    val step = evaluateExpression(statement.substringAfter("+="), delta)
                    node.rotation += step
                }
            }
        } catch (e: Exception) {
            Logger.e("GdScriptEngine", "Error executing '$statement': ${e.message}")
        }
    }

    private fun evaluateExpression(exprStr: String, delta: Float): Float {
        val expr = exprStr.trim()
        if (expr.contains("* delta") || expr.contains("*delta")) {
            val factorStr = expr.replace("* delta", "").replace("*delta", "").trim()
            val factor = getVal(factorStr)
            return factor * delta
        }
        return getVal(expr)
    }

    private fun getVal(token: String): Float {
        val name = token.trim()
        if (variables.containsKey(name)) {
            return (variables[name] as? Number)?.toFloat() ?: 0f
        }
        return name.toFloatOrNull() ?: 0f
    }
}
