package com.example.studio.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.core.EngineConfig
import com.example.engine.core.GameEngine
import com.example.engine.core.GameSurfaceView
import com.example.engine.viewport.ScaleMode
import com.example.studio.engine_bridge.EditorToolMode
import com.example.studio.engine_bridge.StudioRuntimeScene
import com.example.studio.model.GameNode
import com.example.studio.model.GameProject
import com.example.studio.model.NodeType
import com.example.studio.storage.CustomAssetManager
import com.example.studio.storage.ProjectStorageManager

enum class StudioDockTab {
    SCENE_TREE,
    INSPECTOR,
    ASSETS,
    CODE_SCRIPT,
    TILEMAP,
    AI_ASSISTANT,
    CONSOLE,
    EXPORT,
    TEMPLATES
}

@Composable
fun GodotStudioScreen(
    initialProject: GameProject,
    storageManager: ProjectStorageManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Initialize custom asset manager
    LaunchedEffect(Unit) {
        CustomAssetManager.init(context)
    }

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
                if (node != null) {
                    project.selectedNodeId = node.id
                }
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
            if (!isPlayMode) {
                StudioTopAppBar(
                    project = project,
                    isPlayMode = isPlayMode,
                    onTogglePlayMode = {
                        isPlayMode = true
                        runtimeScene.enterPlayMode()
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
            }
        },
        containerColor = Color(0xFF0B1120)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(if (isPlayMode) PaddingValues(0.dp) else innerPadding)
        ) {
            // VIEWPORT AREA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (isPlayMode || !isDockExpanded) 1.0f else 0.52f)
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

                // FULLSCREEN PLAY MODE OVERLAY
                if (isPlayMode) {
                    // Floating Exit / Pause Button
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { runtimeScene.resetPlayMode() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC334155)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restart", fontSize = 12.sp, color = Color.White)
                        }

                        Button(
                            onClick = {
                                isPlayMode = false
                                runtimeScene.enterEditorMode()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xDDDC2626)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("EXIT TO EDITOR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                // EDITOR TOOLS & FLOATING BAR
                if (!isPlayMode) {
                    // Top Left Toolbar: Tools (Move, Brush)
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(Color(0xEE0F172A), shape = RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF334155), shape = RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Select / Move Tool
                        IconButton(
                            onClick = { runtimeScene.activeTool = EditorToolMode.SELECT_MOVE },
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (runtimeScene.activeTool == EditorToolMode.SELECT_MOVE) Color(0xFF0284C7) else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                        ) {
                            Icon(Icons.Default.NearMe, contentDescription = "Select / Move", tint = Color.White, modifier = Modifier.size(16.dp))
                        }

                        // Brush / Quick Placement
                        IconButton(
                            onClick = { runtimeScene.activeTool = EditorToolMode.BRUSH_PLACE },
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (runtimeScene.activeTool == EditorToolMode.BRUSH_PLACE) Color(0xFF10B981) else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                        ) {
                            Icon(Icons.Default.Brush, contentDescription = "Place Brush", tint = Color.White, modifier = Modifier.size(16.dp))
                        }

                        // Brush Presets
                        if (runtimeScene.activeTool == EditorToolMode.BRUSH_PLACE) {
                            listOf(
                                Pair("platform", "Platform"),
                                Pair("coin", "Coin"),
                                Pair("slime", "Slime"),
                                Pair("spike", "Spike")
                            ).forEach { (key, label) ->
                                val isChosen = runtimeScene.brushPreset == key
                                AssistChip(
                                    onClick = { runtimeScene.brushPreset = key },
                                    label = { Text(label, fontSize = 10.sp) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (isChosen) Color(0xFF047857) else Color(0xFF1E293B),
                                        labelColor = Color.White
                                    ),
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }
                    }

                    // Top Right Viewport Controls
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

                    // Dock Expand / Collapse
                    IconButton(
                        onClick = { isDockExpanded = !isDockExpanded },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(34.dp)
                            .background(Color(0xEE0284C7), shape = CircleShape)
                    ) {
                        Icon(
                            if (isDockExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                            contentDescription = "Toggle Dock",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // GODOT STUDIO DOCK PANELS
            if (!isPlayMode && isDockExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.48f)
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
                            text = { Text("Hierarchy", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        Tab(
                            selected = activeDockTab == StudioDockTab.INSPECTOR,
                            onClick = { activeDockTab = StudioDockTab.INSPECTOR },
                            text = { Text("Inspector", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        Tab(
                            selected = activeDockTab == StudioDockTab.ASSETS,
                            onClick = { activeDockTab = StudioDockTab.ASSETS },
                            text = { Text("Assets & FX", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        Tab(
                            selected = activeDockTab == StudioDockTab.CODE_SCRIPT,
                            onClick = { activeDockTab = StudioDockTab.CODE_SCRIPT },
                            text = { Text("Code", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        Tab(
                            selected = activeDockTab == StudioDockTab.TILEMAP,
                            onClick = { activeDockTab = StudioDockTab.TILEMAP },
                            text = { Text("Tilemap", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        Tab(
                            selected = activeDockTab == StudioDockTab.AI_ASSISTANT,
                            onClick = { activeDockTab = StudioDockTab.AI_ASSISTANT },
                            text = { Text("AI Agent", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        Tab(
                            selected = activeDockTab == StudioDockTab.CONSOLE,
                            onClick = { activeDockTab = StudioDockTab.CONSOLE },
                            text = { Text("Console", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        Tab(
                            selected = activeDockTab == StudioDockTab.EXPORT,
                            onClick = { activeDockTab = StudioDockTab.EXPORT },
                            text = { Text("Export", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        Tab(
                            selected = activeDockTab == StudioDockTab.TEMPLATES,
                            onClick = { activeDockTab = StudioDockTab.TEMPLATES },
                            text = { Text("Templates", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(14.dp)) }
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
                            StudioDockTab.ASSETS -> {
                                AssetLibraryDock(
                                    selectedNode = selectedNode,
                                    onAssignAssetToNode = { customAsset ->
                                        selectedNode?.let { node ->
                                            node.customTextureKey = customAsset.id
                                            if (customAsset.type == "spritesheet") {
                                                node.isAnimated = true
                                                node.animationCols = 4
                                                node.animationRows = 1
                                                node.animationFps = 8f
                                            }
                                            runtimeScene.project = project
                                        }
                                    },
                                    onPlaySound = { soundKey ->
                                        gameEngine.audioManager.playSynthSound(soundKey)
                                    }
                                )
                            }
                            StudioDockTab.CODE_SCRIPT -> {
                                CodeEditorDock(
                                    selectedNode = selectedNode,
                                    onNodeUpdated = { updatedNode ->
                                        selectedNode = updatedNode
                                        runtimeScene.project = project
                                    },
                                    onAiFixRequest = { code ->
                                        activeDockTab = StudioDockTab.AI_ASSISTANT
                                    }
                                )
                            }
                            StudioDockTab.TILEMAP -> {
                                TilemapEditorDock(
                                    tilemap = com.example.engine.tilemap.Tilemap(),
                                    onTilemapChanged = {
                                        runtimeScene.project = project
                                    }
                                )
                            }
                            StudioDockTab.AI_ASSISTANT -> {
                                AiAssistantDock(
                                    aiAgent = remember { com.example.studio.ai.StudioAiAgent() },
                                    project = project,
                                    selectedNode = selectedNode,
                                    onProjectUpdated = { updatedProj ->
                                        project = updatedProj
                                        runtimeScene.project = project
                                    },
                                    onScriptGenerated = { generatedScript ->
                                        selectedNode?.let { node ->
                                            node.scriptContent = generatedScript
                                            runtimeScene.project = project
                                        }
                                        activeDockTab = StudioDockTab.CODE_SCRIPT
                                    }
                                )
                            }
                            StudioDockTab.CONSOLE -> {
                                ConsoleDock()
                            }
                            StudioDockTab.EXPORT -> {
                                ExportDock(project = project)
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
                        }
                    }
                }
            }
        }
    }
}
