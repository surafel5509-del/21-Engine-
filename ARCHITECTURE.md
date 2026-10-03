# Apex2D Game Engine Architecture

This document describes the design principles and system hierarchy of the Apex2D Engine and Studio.

```
       +-----------------------------------------------------------+
       |                     Apex2D Editor App                     |
       |  (GodotStudioScreen / Compose Docks / Viewport View)     |
       +----------------------------+------------------------------+
                                    |
          +-------------------------+--------------------------+
          |                                                    |
          v                                                    v
+-----------------------+                            +-------------------+
|  Studio Storage &     |                            |   AI Agent Engine |
|  Project Model        |                            |   (Multi-Provider)|
| (GameProject/GameNode)|                            | (Gemini/OpenAI/..) |
+-----------+-----------+                            +---------+---------+
            |                                                  |
            +-----------------------+--------------------------+
                                    |
                                    v
+----------------------------------------------------------------------+
|                           Apex2D Engine Core                         |
+------------------+-------------------+-------------------+-----------+
| Renderer         | Scene System      | Physics World 2D  | Audio     |
| (Canvas2D)       | (GameObject/Node) | (Collisions/Body) | (Synth)   |
+------------------+-------------------+-------------------+-----------+
| Input Manager    | Camera 2D         | Particles System  | UI System |
| (Touch/Gestures) | (Pan/Zoom)        | (CPU Particles)   | (Canvas)  |
+------------------+-------------------+-------------------+-----------+
```

---

## Architecture Layers

### 1. Engine Core (`com.example.engine`)
Designed strictly independent from the editor framework, responsible for runtime execution:
- **Core Loop & Surface**: `GameEngine`, `GameLoop`, `GameSurfaceView`, high-precision double-buffered 60 FPS tick loop.
- **Rendering**: `Canvas2DRenderer`, camera matrix transformations, batch shape/sprite drawer, z-index sorting, viewport letterboxing.
- **Scene Hierarchy**: `Scene`, `GameObject`, `Component` lifecycle system (`onStart`, `onUpdate`, `onDestroy`).
- **Physics 2D**: Custom lightweight 2D physics solver handling body dynamics, AABB/Circle collisions, impulse resolution, triggers, and raycasting.
- **Particles**: `ParticleEmitterComponent` with CPU particle lifecycle management.
- **Audio**: `AudioManager` synthesized sound generator and PCM audio playback.
- **Scripting Engine**: GDScript/Lua-like lightweight interpreter executing attached scripts and visual rules.

### 2. Studio Model & Storage (`com.example.studio.model`, `com.example.studio.storage`)
- **GameProject**: Master scene project data holding node hierarchy, viewport resolution, gravity, physics parameters, and selected node state.
- **GameNode**: Data structure representing game objects in the project hierarchy with transforms, sprite data, physics attributes, particle settings, visual script rules, and script code.
- **ProjectStorageManager**: JSON & ZIP serialization, saving, loading, export, and import.

### 3. Studio UI & Editor Docks (`com.example.studio.ui`)
Jetpack Compose dark modern UI containing modular docks:
- `SceneTreeDock`: Node hierarchy tree view, node creation, duplicate, delete.
- `InspectorDock`: Property editor for transforms, visual sprites, physics, colliders, particles, and behaviors.
- `AssetLibraryDock`: Asset browser and generator for sprites, audio clips, and prefabs.
- `VisualScriptDock`: Visual logic block builder and GDScript/Lua code editor.
- `TileMapDock`: Interactive tilemap grid painter and layer collision configuration.
- `DebugConsoleDock`: Real-time engine log overlay, filter, error trace, and clear tools.
- `AiAssistantDock`: Built-in AI game development assistant.

### 4. AI Agent Layer (`com.example.studio.ai`)
- **Multi-Provider Architecture**: `AiProvider` interface with implementation adapters for Google Gemini, OpenAI, Anthropic Claude, DeepSeek, OpenAI-Compatible REST API, and Local LLM endpoints.
- **Tool Execution Framework**: `AiEngineToolExecutor` converting AI agent action requests into engine modifications (create node, modify project, generate script, fix code, search project, explain errors, run tests).
