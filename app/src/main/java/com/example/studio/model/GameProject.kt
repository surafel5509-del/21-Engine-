package com.example.studio.model

import java.util.UUID

data class GameProject(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "My 2D Game",
    var description: String = "Created with 2D Game Engine Studio",
    var gravityX: Float = 0f,
    var gravityY: Float = 980f,
    var backgroundColor: Long = 0xFF0F172A, // Dark slate
    var viewportWidth: Float = 1280f,
    var viewportHeight: Float = 720f,
    var gridSnap: Boolean = true,
    var gridSize: Float = 32f,
    var cameraFollowId: String? = null,
    val nodes: MutableList<GameNode> = mutableListOf(),
    var selectedNodeId: String? = null
) {
    fun findNode(id: String?): GameNode? {
        if (id == null) return null
        return nodes.find { it.id == id }
    }

    fun addNode(node: GameNode) {
        nodes.add(node)
        selectedNodeId = node.id
    }

    fun removeNode(id: String) {
        val node = findNode(id) ?: return
        // Remove child references
        nodes.forEach { it.childrenIds.remove(id) }
        nodes.remove(node)
        if (selectedNodeId == id) {
            selectedNodeId = nodes.firstOrNull()?.id
        }
    }
}
