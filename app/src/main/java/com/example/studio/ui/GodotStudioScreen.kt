package com.example.studio.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.core.EngineConfig
import com.example.engine.core.GameEngine
import com.example.engine.core.GameSurfaceView
import com.example.engine.viewport.ScaleMode
import com.example.studio.engine_bridge.StudioRuntimeScene
import com.example.studio.model.GameNode
import com.example.studio.model.GameProject
import com.example.studio.model.NodeType
import com.example.studio.storage.ProjectStorageManager

enum class StudioDockTab {
    SCENE_TREE,
    INSPECTOR,
    VISUAL_SCRIPT,
    TEMPLATES,
    ASSETS
}

@Composable
fun GodotStudioScreen(
    initialProject: GameProject,
    storageManager: ProjectStorageManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var project by remember { mutableStateOf(initialProject) }
    var selectedNode by remember { mutableStateOf(project.findNode(project.selectedNodeId)) }
    var activeDockTab by remember { mutableStateOf(StudioDockTab.SCENE_TREE) }
    var isPlayMode by remember { mutableStateOf(false) }
    var isDockExpanded by remember { mutableStateOf(true) }

    // Game Engine & Scene Instance
    val runtimeScene = remember {
        StudioRuntimeScene(
            project = project,
            onNodeSelectedListener = { node ->
                selectedNode = node
            }
        )
    }

    val gameEngine = remember {
        val config = EngineConfig(
            logicalWidth = 1280f,
            logicalHeight = 720f,
            targetFps = 60,
            scaleMode = ScaleMode.FIT_LETTERBOX,
            isDebugMode = true,
            showFps = true
        )
        GameEngine(context, config).apply {
            setScene(runtimeScene)
            start()
        }
    }

    // Keep scene project in sync
    LaunchedEffect(project) {
        runtimeScene.project = project
        selectedNode = project.findNode(project.selectedNodeId)
    }

    Scaffold(
        topBar = {
            StudioTopAppBar(
                project = project,
                isPlayMode = isPlayMode,
                onTogglePlayMode = {
                    if (isPlayMode) {
                        isPlayMode = false
                        runtimeScene.enterEditorMode()
                    } else {
                        isPlayMode = true
                        runtimeScene.enterPlayMode()
                    }
                },
                onResetPlay = {
                    runtimeScene.resetPlayMode()
                },
                onSaveProject = {
                    storageManager.saveActiveProject(project)
                    Toast.makeText(context, "Game Project Saved!", Toast.LENGTH_SHORT).show()
                },
                onToggleGridSnap = {
                    project = project.copy(gridSnap = !project.gridSnap)
                    runtimeScene.project = project
                }
            )
        },
        containerColor = Color(0xFF0B1120)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // VIEWPORT AREA (AndroidView with GameSurfaceView)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (isPlayMode || !isDockExpanded) 1.0f else 0.55f)
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        GameSurfaceView(ctx).apply {
                            gameEngine.attachView(this)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Floating Editor Controls (Only in Editor Mode)
                if (!isPlayMode) {
                    // Zoom / Center Controls
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                runtimeScene.editorCamera.zoom = (runtimeScene.editorCamera.zoom * 1.25f).coerceAtMost(3.0f)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xCC0F172A), shape = RoundedCornerShape(6.dp))
                        ) {
                            Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = {
                                runtimeScene.editorCamera.zoom = (runtimeScene.editorCamera.zoom / 1.25f).coerceAtLeast(0.4f)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xCC0F172A), shape = RoundedCornerShape(6.dp))
                        ) {
                            Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = {
                                runtimeScene.editorCamera.position.set(640f, 360f)
                                runtimeScene.editorCamera.zoom = 1.0f
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xCC0F172A), shape = RoundedCornerShape(6.dp))
                        ) {
                            Icon(Icons.Default.CenterFocusStrong, contentDescription = "Reset View", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Dock Expand/Collapse Button
                if (!isPlayMode) {
                    IconButton(
                        onClick = { isDockExpanded = !isDockExpanded },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .background(Color(0xCC0284C7), shape = RoundedCornerShape(6.dp))
                    ) {
                        Icon(
                            if (isDockExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                            contentDescription = "Toggle Dock",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // GODOT STUDIO DOCK PANELS (Hierarchy, Inspector, Script, Templates, Assets)
            if (!isPlayMode && isDockExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.45f)
                        .background(Color(0xFF0F172A))
                ) {
                    // Dock Tabs
                    TabRow(
                        selectedTabIndex = activeDockTab.ordinal,
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color(0xFF38BDF8),
                        divider = {}
                    ) {
                        Tab(
                            selected = activeDockTab == StudioDockTab.SCENE_TREE,
                            onClick = { activeDockTab = StudioDockTab.SCENE_TREE },
                            text = { Text("Hierarchy", fontSize = 11.sp) },
                            icon = { Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        Tab(
                            selected = activeDockTab == StudioDockTab.INSPECTOR,
                            onClick = { activeDockTab = StudioDockTab.INSPECTOR },
                            text = { Text("Inspector", fontSize = 11.sp) },
                            icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        Tab(
                            selected = activeDockTab == StudioDockTab.VISUAL_SCRIPT,
                            onClick = { activeDockTab = StudioDockTab.VISUAL_SCRIPT },
                            text = { Text("Script", fontSize = 11.sp) },
                            icon = { Icon(Icons.Default.Javascript, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        Tab(
                            selected = activeDockTab == StudioDockTab.TEMPLATES,
                            onClick = { activeDockTab = StudioDockTab.TEMPLATES },
                            text = { Text("Templates", fontSize = 11.sp) },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        Tab(
                            selected = activeDockTab == StudioDockTab.ASSETS,
                            onClick = { activeDockTab = StudioDockTab.ASSETS },
                            text = { Text("Audio FX", fontSize = 11.sp) },
                            icon = { Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }

                    // Dock Content
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (activeDockTab) {
                            StudioDockTab.SCENE_TREE -> {
                                SceneTreeDock(
                                    project = project,
                                    selectedNodeId = project.selectedNodeId,
                                    onSelectNode = { node ->
                                        project.selectedNodeId = node.id
                                        selectedNode = node
                                        runtimeScene.project = project
                                    },
                                    onAddNode = { nodeType ->
                                        val newNode = GameNode(
                                            name = "Node_${project.nodes.size + 1}",
                                            nodeType = nodeType,
                                            posX = runtimeScene.editorCamera.position.x,
                                            posY = runtimeScene.editorCamera.position.y,
                                            hasPhysics = (nodeType == NodeType.RIGID_BODY_2D || nodeType == NodeType.STATIC_BODY_2D),
                                            bodyType = if (nodeType == NodeType.STATIC_BODY_2D) "STATIC" else "DYNAMIC",
                                            isParticleEmitter = (nodeType == NodeType.CPU_PARTICLES_2D)
                                        )
                                        project.addNode(newNode)
                                        selectedNode = newNode
                                        runtimeScene.project = project
                                        activeDockTab = StudioDockTab.INSPECTOR
                                    },
                                    onDeleteNode = { nodeId ->
                                        project.removeNode(nodeId)
                                        selectedNode = project.findNode(project.selectedNodeId)
                                        runtimeScene.project = project
                                    },
                                    onDuplicateNode = { node ->
                                        val copy = node.duplicate()
                                        copy.posX += 32f
                                        copy.posY += 32f
                                        project.addNode(copy)
                                        selectedNode = copy
                                        runtimeScene.project = project
                                    }
                                )
                            }
                            StudioDockTab.INSPECTOR -> {
                                InspectorDock(
                                    selectedNode = selectedNode,
                                    onNodeUpdated = { updatedNode ->
                                        selectedNode = updatedNode
                                        runtimeScene.project = project
                                    }
                                )
                            }
                            StudioDockTab.VISUAL_SCRIPT -> {
                                VisualScriptDock(
                                    selectedNode = selectedNode,
                                    onNodeUpdated = { updatedNode ->
                                        selectedNode = updatedNode
                                        runtimeScene.project = project
                                    }
                                )
                            }
                            StudioDockTab.TEMPLATES -> {
                                TemplatesDock(
                                    onLoadTemplate = { templateProj ->
                                        project = templateProj
                                        selectedNode = project.findNode(project.selectedNodeId)
                                        runtimeScene.project = project
                                        storageManager.saveActiveProject(project)
                                        Toast.makeText(context, "Loaded ${templateProj.name}!", Toast.LENGTH_SHORT).show()
                                        activeDockTab = StudioDockTab.SCENE_TREE
                                    }
                                )
                            }
                            StudioDockTab.ASSETS -> {
                                AssetLibraryDock(
                                    onPlaySound = { soundKey ->
                                        gameEngine.audioManager.playSynthSound(soundKey)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
