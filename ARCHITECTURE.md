# Godot Studio 2D Architecture

## System Architecture Diagram

```
+-------------------------------------------------------------------------+
|                         Android Application Layer                       |
|                             (MainActivity)                              |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                      Godot Studio Editor (UI / Dock)                     |
|  +--------------+  +--------------+  +--------------+  +--------------+ |
|  | Hierarchy    |  | Inspector    |  | Code.gd      |  | Visual Logic | |
|  +--------------+  +--------------+  +--------------+  +--------------+ |
|  +--------------+  +--------------+  +--------------+  +--------------+ |
|  | Asset Lib    |  | AI Copilot   |  | Debug Console|  | Templates    | |
|  +--------------+  +--------------+  +--------------+  +--------------+ |
+-------------------------------------------------------------------------+
          |                         |                         |
          v                         v                         v
+------------------+      +------------------+      +------------------+
| Studio Engine    |      | Pluggable AI     |      | Android Export   |
| Bridge & Scene   |      | Layer            |      | System           |
| (StudioRuntime)  |      | (com.studio.ai)  |      | (com.export)     |
+------------------+      +------------------+      +------------------+
          |                         |
          v                         v
+-------------------------------------------------------------------------+
|                           Engine Core Subsystems                        |
| +--------------+  +--------------+  +--------------+  +---------------+ |
| | Renderer     |  | Physics2D    |  | Audio System |  | Input Manager | |
| +--------------+  +--------------+  +--------------+  +---------------+ |
| +--------------+  +--------------+  +--------------+  +---------------+ |
| | Particles2D  |  | Camera2D     |  | Viewport     |  | Asset Manager | |
| +--------------+  +--------------+  +--------------+  +---------------+ |
+-------------------------------------------------------------------------+
```

---

## Module Breakdown

1. **Engine Core (`com.example.engine.core`)**:
   - `GameEngine`: High-level controller managing game loop thread, surface view lifecycle, active scene, and viewport scaling.
   - `GameLoop`: Precise 60 FPS update-render loop.
   - `Time`: Delta time, fixed delta time, and frame counting utilities.

2. **Rendering Engine (`com.example.engine.rendering`)**:
   - `Renderer`: Canvas2D rendering pipeline handling sprite drawing, procedural geometry, tilemaps, particle FX, and camera transforms.

3. **Physics Engine (`com.example.engine.physics`)**:
   - `PhysicsWorld2D`: Broadphase and narrowphase AABB & circle collision resolution with gravity, restitution, friction, and trigger event dispatch.

4. **Scene & Node System (`com.example.engine.scene` & `com.example.studio.model`)**:
   - `Scene`: Scene lifecycle (`onCreate`, `onUpdate`, `onRender`, `onTouch`).
   - `GameNode`: Serializable scene node model containing transform, sprite, physics, behavior, particle, and visual script configuration.

5. **Pluggable AI Agent (`com.example.studio.ai`)**:
   - `AiProvider`: Interface abstracting AI backends (`OpenAI`, `Google Gemini`, `Anthropic Claude`, `DeepSeek`, `OpenAI-Compatible Local LLMs`, and `LocalHeuristicProvider` for offline operation).
   - `AiAgent`: Intelligent agent processing user prompts, parsing structured engine actions, generating scripts, explaining errors, and modifying scene projects.
   - `AiDock`: Interactive Compose UI dock for prompt chat, provider selection, action preview, and execution.

6. **Scripting Systems (`com.example.studio.ui`)**:
   - `CodeScriptDock`: Text-based GDScript / Lua-like code editor with syntax formatting and code templates.
   - `VisualScriptDock`: Node-based signal event-action rule builder.

7. **Android Export System (`com.example.studio.export`)**:
   - `AndroidExportSystem`: Simulates and compiles standalone APK project bundles directly on Android.
   - `ExportApkModal`: Configuration dialog for package names, versions, and build triggering.
