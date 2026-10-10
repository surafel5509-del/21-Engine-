# Technical Architecture Specification

## Overview

The engine and editor are designed with strict separation between runtime game logic (`com.example.engine`) and editor interface (`com.example.studio`), connected via an abstraction layer (`com.example.studio.engine_bridge` and `com.example.studio.ai`).

---

## 1. Engine Core (`com.example.engine`)

### Core Loop & Viewport
- `GameEngine`: High-level controller managing game loop lifecycle, scenes, viewport scaling, and input dispatching.
- `GameLoop`: Fixed timestep loop ensuring smooth 60 FPS update and render execution.
- `ViewportManager`: Handles resolution letterboxing, scaling modes, and aspect ratio matching.

### Rendering (`com.example.engine.rendering`)
- `Renderer` (Interface) & `Canvas2DRenderer`: Zero-allocation Android Canvas renderer utilizing paint pooling, cached matrices, and batch draw operations.
- `Camera2D`: Position, zoom, screen-shake, rotation, and viewport transformations.
- `Sprite`: Sprite frames, pivot offsets, flip modes, and texture clipping.

### Scene & Node Hierarchy (`com.example.engine.scene` & `components`)
- `GameNode` & `NodeType`: Hierarchical node representation supporting 2D transforms, z-indexing, parent-child relationships, and tags.
- Components:
  - `SpriteRendererComponent` & `AnimatedSpriteComponent`
  - `RigidBody2DComponent` & `Collider2DComponent`
  - `ParticleEmitterComponent`
  - `TextRendererComponent`
  - `TilemapComponent`: Grid-based tile rendering and tilemap collisions.
  - `ScriptComponent`: Execution container for text scripts and visual scripting rules.

### Physics (`com.example.engine.physics`)
- `PhysicsWorld2D`: Lightweight 2D physics engine handling AABB box and circle collision detection, manifold resolution, impulse calculation, gravity, bounciness, and friction.

### Scripting (`com.example.engine.scripting`)
- `ScriptEngine` & `ScriptEnvironment`: Lightweight GDScript/Lua-like text script interpreter and AST evaluator executing lifecycle methods (`_ready`, `_process`), modifying node positions, applying forces, spawning prefabs, and triggering audio.

### Audio (`com.example.engine.audio`)
- `AudioManager`: Android SoundPool & MediaPlayer wrapper combined with a real-time procedural Sound Synthesizer generating retro 8-bit game audio.

---

## 2. Editor Framework (`com.example.studio`)

### Model & Storage
- `GameProject`: Data structure representing project metadata, global gravity, nodes, and scenes.
- `ProjectStorageManager`: JSON serialization and storage manager saving active projects locally.
- `CustomAssetManager`: Asset registry managing textures, spritesheets, audio files, and tile maps.

### UI Docks (`com.example.studio.ui`)
- `SceneTreeDock`: Node hierarchy tree viewer with add, delete, duplicate, reorder operations.
- `InspectorDock`: Real-time node property inspector (Transforms, Physics, Colors, Sprites, Particles, Behaviors).
- `CodeEditorDock`: GDScript text editor with syntax highlighting, script template generator, and error linting.
- `TilemapDock`: Interactive visual tile painter with layer selection and palette management.
- `AssetLibraryDock`: Asset manager displaying graphics, audio, and particle presets.
- `VisualScriptDock`: Rule-based visual logic block editor.
- `DebugConsoleDock`: System logs, execution metrics, and scripting stack traces.
- `ProjectManagerDock`: Project loader, JSON export/import, and APK builder interface.

---

## 3. Pluggable AI System (`com.example.studio.ai`)

### Interfaces & Providers
- `AiProvider`: Pluggable contract for AI language models (`sendMessage(prompt, context)`).
- Implementations:
  - `OpenAiProvider`
  - `GeminiProvider`
  - `ClaudeProvider`
  - `DeepSeekProvider`
  - `OpenAiCompatibleProvider`
  - `LocalHeuristicAiProvider`: Smart offline local AI copilot operating without network connectivity.

### Agent & Tool Calls
- `AiAgent`: Tool execution controller translating natural language requests into project mutations:
  - `createNode()`
  - `modifyNodeProperty()`
  - `generateScript()`
  - `fixScriptError()`
  - `searchProject()`
  - `runBuildTests()`
