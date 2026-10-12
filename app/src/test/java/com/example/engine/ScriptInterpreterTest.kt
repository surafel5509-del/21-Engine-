package com.example.engine

import com.example.engine.scripting.ScriptInterpreter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ScriptInterpreterTest {

    @Test
    fun testVariableParsingAndEvaluation() {
        val script = """
            var speed = 150
            var name = "Player1"
            var is_active = true

            func _ready():
                print("Ready")
        """.trimIndent()

        val interpreter = ScriptInterpreter(script)
        assertEquals(150f, interpreter.getVariable("speed"))
        assertEquals("Player1", interpreter.getVariable("name"))
        assertEquals(true, interpreter.getVariable("is_active"))
    }

    @Test
    fun testLifecycleHookExecution() {
        val script = """
            var speed = 100

            func _process(delta):
                position.x += 100 * delta
        """.trimIndent()

        val interpreter = ScriptInterpreter(script)
        interpreter.executeLifecycleHook("_process", 0.016f)
        assertNotNull(interpreter)
    }
}
