# Architecture Documentation

## Overview

The application is structured into three clean, decoupled layers:

```
[ Android Application Layer (MainActivity, Theme) ]
                     │
        ┌────────────┴────────────┐
        ▼                         ▼
[ Editor Studio Layer ]   [ AI Assistant Layer ]
(com.example.studio)      (com.example.studio.ai)
        │                         │
        └────────────┬────────────┘
                     ▼
       [ Runtime Engine Core Layer ]
          (com.example.engine)
```

1. **Engine Core Layer (`com.example.engine`)**: Pure 2D game engine implementation with zero dependencies on editor UI. Can run standalone games in a surface view or canvas.
2. **Editor Studio Layer (`com.example.studio`)**: Touch-first visual game studio built with Jetpack Compose. Houses node hierarchies, inspectors, visual logic editors, asset browsers, and project persistence.
3. **AI Assistant Layer (`com.example.studio.ai`)**: Provider-agnostic AI agent system capable of creating scenes, modifying node parameters, writing logic scripts, explaining errors, and searching project structures.

---

## Engine Core Architecture

### Renderer & Viewport
- **`Canvas2DRenderer`**: Handles double-buffered Canvas rendering with hardware acceleration where available. Supports layer sorting, camera transformations, batching shape draws, and tinting.
- **`ViewportManager` & `ScaleMode`**: Supports `FIT_LETTERBOX`, `FILL_CROP`, and `MATCH_EXACT` scaling options for device screens of varying aspect ratios.
- **`Camera2D`**: Translates world coordinates into viewport screen space with smooth tracking, zoom, and bounds clamping.

### Scene Graph & ECS Component System
- **`SceneManager` & `Scene`**: Manages lifecycle, scene transitions, update loops, and active game objects.
- **`GameObject` & `Component`**:
  - `SpriteRendererComponent`
  - `AnimatedSpriteComponent`
  - `RigidBody2DComponent`
  - `Collider2DComponent`
  - `ParticleEmitterComponent`
  - `ScriptComponent`
  - `TextRendererComponent`
  - `ShapeRendererComponent`

### Physics & Collision Processing
- **`PhysicsWorld2D`**: Manages gravity, velocity integration, AABB bounding box collision checks, impulse resolution, friction, and trigger callbacks.
- Supports `DYNAMIC`, `STATIC`, and `KINEMATIC` rigid body types.

### Audio Manager
- **`AudioManager`**: Synthesizes real-time sound effects (jumps, hits, coins, explosions, powerups) using Android `AudioTrack` and wave generation without external audio files.

---

## Editor Studio Architecture

### Data Models & Storage
- **`GameProject`**: Container for scene settings, screen dimensions, gravity vector, grid snapping, and node tree.
- **`GameNode`**: Node tree representation containing transform properties, sprite key, physics settings, visual script logic, and particle parameters.
- **`ProjectStorageManager`**: JSON serialization and file persistence in Android internal storage.

### Studio UI Docks (Jetpack Compose)
- **`GodotStudioScreen`**: Top-level container hosting viewport SurfaceView and docking panels.
- **`SceneTreeDock`**: Scene hierarchy navigation, re-ordering, duplicate/add/delete actions.
- **`InspectorDock`**: Live attribute inspector (position, scale, rotation, color, body type, collision bounds).
- **`AssetLibraryDock`**: Asset preview, procedural FX synthesizer, and sprite sheet slice config.
- **`VisualScriptDock`**: Visual node trigger configurer (`OnTouch`, `OnCollision`, `AutoMove`, `Spawner`, `Bounce`).
- **`TemplatesDock`**: Pre-configured game projects (Platformer, Physics Sandbox, Stress Benchmark).
- **`AiAssistantDock`**: Interactive AI chat, provider selector, and action preview.

---

## AI Assistant Layer Architecture

The AI layer is designed with a strict interface abstraction so that any model provider (cloud or local) can be plugged in seamlessly:

```
                  ┌──────────────────────┐
                  │     AiProvider       │ (Interface)
                  └──────────┬───────────┘
                             │
       ┌─────────────────────┼─────────────────────┐
       ▼                     ▼                     ▼
[FirebaseGeminiProvider] [OpenAiCompatible] [RuleBasedLocalAiProvider]
 (Gemini API)             (GPT/Claude/DeepSeek) (Offline local engine)
```

### Key Components

- **`AiProvider` Interface:**
  - `fun sendMessage(prompt: String, context: AiContext): Flow<AiResponse>`
  - `fun getProviderName(): String`
  - `fun isOfflineSupported(): Boolean`

- **`EngineAiAssistant`:**
  - Translates user intent into structured engine actions (`AiAction`).
  - Supported Actions:
    - `CreateNodeAction(name, nodeType, x, y)`
    - `ModifyNodePropertyAction(nodeId, property, value)`
    - `AttachScriptAction(nodeId, scriptLogic)`
    - `ExplainErrorAction(errorMessage)`
    - `SearchProjectAction(query)`
  - Executes approved actions directly against active `GameProject` or `StudioRuntimeScene`.
