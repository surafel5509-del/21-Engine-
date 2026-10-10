package com.example.studio.ai.providers

import com.example.studio.ai.AiProvider

class LocalHeuristicAiProvider : AiProvider {
    override val id: String = "local_heuristic"
    override val displayName: String = "Offline Local Game Copilot"
    override val requiresApiKey: Boolean = false
    override val isOfflineSupported: Boolean = true

    override suspend fun sendMessage(prompt: String, projectContext: String, apiKey: String): String {
        val lower = prompt.lowercase()
        val sb = StringBuilder()

        sb.append("🤖 [Offline AI Copilot Response]\n")

        when {
            lower.contains("player") || lower.contains("hero") -> {
                sb.append("ACTION: CREATE_NODE|Player|CHARACTER_BODY_2D|player\n")
                sb.append("I have generated a Player Character node pre-configured with Platformer Player movement behavior and 2D physics.")
            }
            lower.contains("enemy") || lower.contains("slime") -> {
                sb.append("ACTION: CREATE_NODE|SlimeEnemy|CHARACTER_BODY_2D|enemy_patrol\n")
                sb.append("I have generated an Enemy Patrol node that automatically moves back and forth in the scene.")
            }
            lower.contains("coin") || lower.contains("gem") -> {
                sb.append("ACTION: CREATE_NODE|CoinGold|AREA_2D|coin\n")
                sb.append("I have generated a Collectible Coin node with trigger collision and sound effect triggers.")
            }
            lower.contains("script") || lower.contains("code") -> {
                sb.append("ACTION: GENERATE_SCRIPT|func _ready():\n    print(\"Script attached\")\n    play_sound(\"jump\")\n\nfunc _process(delta):\n    position_x += move_speed * delta\n")
                sb.append("Generated a new GDScript with _ready() and _process(delta) hooks.")
            }
            lower.contains("fix") || lower.contains("error") -> {
                sb.append("ACTION: FIX_ERRORS\n")
                sb.append("Scanned GDScript files and verified syntax structure. Fixed missing ':' after function declarations.")
            }
            lower.contains("search") || lower.contains("find") -> {
                val query = prompt.substringAfter("search").substringAfter("find").trim()
                sb.append("ACTION: SEARCH|$query\n")
                sb.append("Searching project for '$query'...")
            }
            else -> {
                sb.append("Analyzed your request: '$prompt'.\n")
                sb.append("Suggested steps:\n")
                sb.append("1. Add game nodes using Hierarchy or Brush tool.\n")
                sb.append("2. Edit GDScript logic in the Script tab.\n")
                sb.append("3. Click 'Run Game' to test physics and collisions.")
            }
        }

        return sb.toString()
    }
}
