# System Architecture

## Architecture Overview

The system is designed with a strict modular architecture separating the **Engine Core** (runtime) from the **Studio Editor** (UI & tooling) and the **AI Agent Layer**.

```
+-------------------------------------------------------------------------+
|                              STUDIO EDITOR                              |
|  +------------------+  +-------------------+  +----------------------+  |
|  | GodotStudioScreen|  |   Inspector Dock  |  |  Scene Tree Dock     |  |
|  +------------------+  +-------------------+  +----------------------+  |
|  | Visual Script Dock| | Asset Library Dock|  | Project Storage Mgr  |  |
|  +------------------+  +-------------------+  +----------------------+  |
+------------------------------------+------------------------------------+
                                     |
                                     v
+-------------------------------------------------------------------------+
|                              ENGINE CORE                                |
|  +------------------+  +-------------------+  +----------------------+  |
|  |   GameEngine     |  |   SceneManager    |  |     GameLoop         |  |
|  +------------------+  +-------------------+  +----------------------+  |
|  | Canvas2DRenderer |  |   PhysicsWorld2D  |  |    Camera2D          |  |
|  +------------------+  +-------------------+  +----------------------+  |
|  |   AudioManager   |  |   AssetManager    |  |   InputManager       |  |
|  +------------------+  +-------------------+  +----------------------+  |
+------------------------------------+------------------------------------+
                                     ^
                                     |
+------------------------------------+------------------------------------+
|                               AI LAYER                                  |
|  +-------------------------------------------------------------------+  |
|  |                           AiProvider                              |  |
|  | +------------------+ +------------------+ +---------------------+ |  |
|  | | OpenAI / Claude  | | Gemini / DeepSeek| | Local Heuristic     | |  |
|  | +------------------+ +------------------+ +---------------------+ |  |
|  +-------------------------------------------------------------------+  |
+-------------------------------------------------------------------------+
```

## Core Modules

### 1. Engine Core (`com.example.engine`)
- **GameEngine**: Central controller managing loop ticks, fixed updates, rendering, and lifecycle.
- **SceneManager & GameObject**: Node hierarchy graph where every node is a `GameObject` with attached `Component`s (Transform2D, SpriteRenderer, RigidBody2D, Collider2D, Script, Audio, ParticleEmitter, etc.).
- **Canvas2DRenderer**: Multi-pass Android Canvas batch renderer with Z-index ordering, camera projections, scale modes, and debug overlays.
- **PhysicsWorld2D**: AABB and Circle collision detector, spatial partitioning, impulse solver, and gravity simulation.
- **AudioManager**: Dual-track audio system supporting sound effects (`SoundPool`) and background music streaming (`MediaPlayer`).
- **Camera2D**: Dynamic 2D camera with target tracking, smooth dampening, viewport boundary constraints, zoom, and screen shake effects.
- **InputManager**: Touch gesture recognizer, multi-touch virtual joysticks, directional pads, and screen buttons.

### 2. Studio Editor (`com.example.studio`)
- **GodotStudioScreen**: Modern dark-themed Jetpack Compose workspace with dockable panels (Scene Tree, Inspector, Visual Scripting, Asset Manager, Code Editor, Project Settings).
- **StudioRuntimeScene**: Bridge converting Studio project JSON metadata (`GameProject`, `GameNode`) into active Engine `GameObject` hierarchy for real-time play testing.
- **ProjectStorageManager**: File-system persistence layer handling project serialization (`project.json`, scenes, scripts, assets).

### 3. AI Agent Layer (`com.example.studio.ai` / `com.example.studio.ui.AiAssistantDock`)
- **AiProvider Interface**: Standardized protocol for AI models (`generateScript()`, `explainError()`, `modifyScene()`, `suggestFix()`).
- **Pluggable Implementations**:
  - `OpenAiProvider` (OpenAI API / ChatGPT)
  - `GeminiProvider` (Google Gemini API)
  - `ClaudeProvider` (Anthropic Claude API)
  - `DeepSeekProvider` (DeepSeek API)
  - `LocalHeuristicAiProvider` (Offline fallback agent operating locally on device without network)
- Dynamic provider switcher via runtime configuration.
