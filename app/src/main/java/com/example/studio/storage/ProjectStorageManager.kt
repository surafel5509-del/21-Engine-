package com.example.studio.storage

import android.content.Context
import com.example.studio.model.*
import com.example.studio.templates.ProjectTemplates
import org.json.JSONArray
import org.json.JSONObject

class ProjectStorageManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("godot_studio_prefs", Context.MODE_PRIVATE)

    fun loadActiveProject(): GameProject {
        val savedJson = prefs.getString("active_project_json", null)
        if (!savedJson.isNullOrEmpty()) {
            try {
                return deserializeProject(savedJson)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Default to Platformer template
        return ProjectTemplates.createPlatformerTemplate()
    }

    fun saveActiveProject(project: GameProject) {
        val jsonStr = serializeProject(project)
        prefs.edit().putString("active_project_json", jsonStr).apply()
    }

    fun serializeProject(project: GameProject): String {
        val root = JSONObject()
        root.put("id", project.id)
        root.put("name", project.name)
        root.put("description", project.description)
        root.put("gravityX", project.gravityX.toDouble())
        root.put("gravityY", project.gravityY.toDouble())
        root.put("backgroundColor", project.backgroundColor)
        root.put("gridSnap", project.gridSnap)
        root.put("gridSize", project.gridSize.toDouble())
        root.put("cameraFollowId", project.cameraFollowId ?: "")
        root.put("selectedNodeId", project.selectedNodeId ?: "")

        val nodesArray = JSONArray()
        project.nodes.forEach { node ->
            val nObj = JSONObject()
            nObj.put("id", node.id)
            nObj.put("name", node.name)
            nObj.put("nodeType", node.nodeType.name)
            nObj.put("tag", node.tag)
            nObj.put("posX", node.posX.toDouble())
            nObj.put("posY", node.posY.toDouble())
            nObj.put("scaleX", node.scaleX.toDouble())
            nObj.put("scaleY", node.scaleY.toDouble())
            nObj.put("rotation", node.rotation.toDouble())
            nObj.put("isVisible", node.isVisible)
            nObj.put("isLocked", node.isLocked)
            nObj.put("spriteShape", node.spriteShape)
            nObj.put("color", node.color)
            nObj.put("alpha", node.alpha.toDouble())
            nObj.put("width", node.width.toDouble())
            nObj.put("height", node.height.toDouble())
            nObj.put("hasPhysics", node.hasPhysics)
            nObj.put("bodyType", node.bodyType)
            nObj.put("mass", node.mass.toDouble())
            nObj.put("bounciness", node.bounciness.toDouble())
            nObj.put("friction", node.friction.toDouble())
            nObj.put("gravityScale", node.gravityScale.toDouble())
            nObj.put("fixedRotation", node.fixedRotation)
            nObj.put("isTrigger", node.isTrigger)
            nObj.put("colliderShape", node.colliderShape.name)
            nObj.put("colliderWidth", node.colliderWidth.toDouble())
            nObj.put("colliderHeight", node.colliderHeight.toDouble())
            nObj.put("colliderRadius", node.colliderRadius.toDouble())
            nObj.put("isParticleEmitter", node.isParticleEmitter)
            nObj.put("emissionRate", node.emissionRate.toDouble())
            nObj.put("maxParticles", node.maxParticles)
            nObj.put("behavior", node.behavior.name)
            nObj.put("moveSpeed", node.moveSpeed.toDouble())
            nObj.put("jumpForce", node.jumpForce.toDouble())
            nObj.put("patrolDistance", node.patrolDistance.toDouble())
            nObj.put("orbitSpeed", node.orbitSpeed.toDouble())
            nObj.put("scoreValue", node.scoreValue)
            nObj.put("soundOnAction", node.soundOnAction)

            nodesArray.put(nObj)
        }
        root.put("nodes", nodesArray)

        return root.toString()
    }

    fun deserializeProject(jsonStr: String): GameProject {
        val root = JSONObject(jsonStr)
        val project = GameProject(
            id = root.optString("id", "proj_default"),
            name = root.optString("name", "My 2D Game"),
            description = root.optString("description", ""),
            gravityX = root.optDouble("gravityX", 0.0).toFloat(),
            gravityY = root.optDouble("gravityY", 980.0).toFloat(),
            backgroundColor = root.optLong("backgroundColor", 0xFF0F172AL),
            gridSnap = root.optBoolean("gridSnap", true),
            gridSize = root.optDouble("gridSize", 32.0).toFloat(),
            cameraFollowId = root.optString("cameraFollowId", "").takeIf { it.isNotEmpty() },
            selectedNodeId = root.optString("selectedNodeId", "").takeIf { it.isNotEmpty() }
        )

        val nodesArray = root.optJSONArray("nodes") ?: JSONArray()
        for (i in 0 until nodesArray.length()) {
            val nObj = nodesArray.getJSONObject(i)
            val node = GameNode(
                id = nObj.optString("id"),
                name = nObj.optString("name", "Node"),
                nodeType = try { NodeType.valueOf(nObj.optString("nodeType", "SPRITE_2D")) } catch (e: Exception) { NodeType.SPRITE_2D },
                tag = nObj.optString("tag", "Untagged"),
                posX = nObj.optDouble("posX", 640.0).toFloat(),
                posY = nObj.optDouble("posY", 360.0).toFloat(),
                scaleX = nObj.optDouble("scaleX", 1.0).toFloat(),
                scaleY = nObj.optDouble("scaleY", 1.0).toFloat(),
                rotation = nObj.optDouble("rotation", 0.0).toFloat(),
                isVisible = nObj.optBoolean("isVisible", true),
                isLocked = nObj.optBoolean("isLocked", false),
                spriteShape = nObj.optString("spriteShape", "box"),
                color = nObj.optLong("color", 0xFF0284C7L),
                alpha = nObj.optDouble("alpha", 1.0).toFloat(),
                width = nObj.optDouble("width", 64.0).toFloat(),
                height = nObj.optDouble("height", 64.0).toFloat(),
                hasPhysics = nObj.optBoolean("hasPhysics", false),
                bodyType = nObj.optString("bodyType", "DYNAMIC"),
                mass = nObj.optDouble("mass", 1.0).toFloat(),
                bounciness = nObj.optDouble("bounciness", 0.2).toFloat(),
                friction = nObj.optDouble("friction", 0.5).toFloat(),
                gravityScale = nObj.optDouble("gravityScale", 1.0).toFloat(),
                fixedRotation = nObj.optBoolean("fixedRotation", false),
                isTrigger = nObj.optBoolean("isTrigger", false),
                colliderShape = try { ColliderShapeType.valueOf(nObj.optString("colliderShape", "BOX")) } catch (e: Exception) { ColliderShapeType.BOX },
                colliderWidth = nObj.optDouble("colliderWidth", 64.0).toFloat(),
                colliderHeight = nObj.optDouble("colliderHeight", 64.0).toFloat(),
                colliderRadius = nObj.optDouble("colliderRadius", 32.0).toFloat(),
                isParticleEmitter = nObj.optBoolean("isParticleEmitter", false),
                emissionRate = nObj.optDouble("emissionRate", 40.0).toFloat(),
                maxParticles = nObj.optInt("maxParticles", 100),
                behavior = try { BehaviorType.valueOf(nObj.optString("behavior", "NONE")) } catch (e: Exception) { BehaviorType.NONE },
                moveSpeed = nObj.optDouble("moveSpeed", 300.0).toFloat(),
                jumpForce = nObj.optDouble("jumpForce", 650.0).toFloat(),
                patrolDistance = nObj.optDouble("patrolDistance", 200.0).toFloat(),
                orbitSpeed = nObj.optDouble("orbitSpeed", 90.0).toFloat(),
                scoreValue = nObj.optInt("scoreValue", 100),
                soundOnAction = nObj.optString("soundOnAction", "jump")
            )
            project.nodes.add(node)
        }

        return project
    }
}
