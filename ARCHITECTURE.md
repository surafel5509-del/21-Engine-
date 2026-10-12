# System Architecture - Android 2D Game Engine & Studio

## System Overview

The project is structured into three clean, decoupled layers:

```
+-------------------------------------------------------------------+
|                        Studio UI Layer                            |
| (Composables, Scene Tree Dock, Inspector, Code Editor, AI Dock)   |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                        AI Agent Layer                             |
| (AiProvider, StudioAiAgent, LocalHeuristic, OpenAI/Gemini/Claude) |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                        Engine Core Layer                          |
| (GameLoop, Canvas2DRenderer, PhysicsWorld2D, ScriptInterpreter,   |
|  TilemapSystem, ParticleEmitter, AudioManager, PrefabManager)     |
+-------------------------------------------------------------------+
```

## Layer Architecture & Component Breakdown

### 1. Engine Core Layer (`com.example.engine`)
- **Core & Runtime**: `GameLoop`, `GameEngine`, `GameSurfaceView`, `Time`.
- **Renderer**: `Canvas2DRenderer`, `Renderer`, `Sprite`, `RenderLayer`.
- **Viewport & Camera**: `ViewportManager`, `Camera2D`, `ScaleMode`.
- **Physics**: `PhysicsWorld2D`, SAT/AABB collisions, rigid bodies, impulse resolution.
- **Components**: `SpriteRendererComponent`, `AnimatedSpriteComponent`, `RigidBody2DComponent`, `Collider2DComponent`, `ParticleEmitterComponent`, `TilemapComponent`, `ScriptComponent`.
- **Scripting Engine**: `ScriptInterpreter` - GDScript/Lua runtime interpreter with variable binding, arithmetic parser, and method dispatch.
- **Audio & Particles**: `AudioManager`, sound synthesizer, particle systems.

### 2. Visual Studio Layer (`com.example.studio`)
- **Node & Project Model**: `GameProject`, `GameNode`, `NodeType`, `Prefab`.
- **Engine Bridge**: `StudioRuntimeScene`, converts UI `GameProject` node graph into active runtime engine GameObjects and Physics bodies.
- **Editor UI Docks**:
  - `SceneTreeDock`: Hierarchical scene graph manager.
  - `InspectorDock`: Live property modifier.
  - `CodeEditorDock`: Multi-file script editor with syntax checking and AI fix triggers.
  - `TilemapEditorDock`: Palette and painter for tile grids.
  - `AssetLibraryDock`: Asset browser and synthesizer sound preview.
  - `AiAssistantDock`: Chat interface, provider switcher, automated scene and script modifier.
  - `ConsoleDock`: Engine runtime logs and exception stack traces.
  - `ExportDock`: Standalone Android APK build manager.

### 3. AI Agent Architecture (`com.example.studio.ai`)
- **`AiProvider` Interface**: Common strategy pattern defining `generateScript`, `explainError`, `modifyScene`, `createObject`.
- **Provider Implementations**:
  - `OpenAiProvider`
  - `GeminiProvider`
  - `ClaudeProvider`
  - `DeepSeekProvider`
  - `OpenAiCompatibleProvider`
  - `LocalHeuristicProvider` (Offline fallback using rule-based AST generation)
- **`StudioAiAgent`**: Orchestrator executing AI decisions against `GameProject` models and file systems.
