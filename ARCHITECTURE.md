# ARCHITECTURE.md - System Architecture & Design

This document details the software architecture of the Android 2D Game Engine and Studio.

## Architecture Layers

```
+-------------------------------------------------------------------+
|                        Godot Studio UI                            |
|  (GodotStudioScreen, Inspector, SceneTree, CodeEditor, AI Dock)   |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                           AI Layer                                |
|    (AiGameAgent, AiProvider: OpenAI, Gemini, Claude, DeepSeek)     |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                      Export & Storage System                      |
|            (ApkExporter, ProjectStorageManager, PrefabManager)   |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                         Engine Core                               |
| (GameEngine, GdScriptEngine, PhysicsWorld2D, Canvas2DRenderer)    |
+-------------------------------------------------------------------+
```

## 1. Engine Core (`com.example.engine`)

- **GameEngine / GameLoop:** Core tick execution system decoupled from UI frame rates. Handles fixed timestep physics updates and continuous rendering loops.
- **GdScriptEngine:** Interpreter for GDScript/Lua-like code blocks attached to game nodes. Supports `_ready()`, `_process(delta)`, node vector manipulation, and signal handling.
- **PhysicsWorld2D:** AABB and Circle collision resolution engine supporting dynamic rigidbodies, static colliders, triggers, friction, bounciness, and impulse forces.
- **Canvas2DRenderer:** Canvas-based 2D renderer supporting hardware acceleration, Z-index sorting, sprite sheets, tilemaps, and particle blending.
- **TilemapLayer:** Grid-based tilemap subsystem for level rendering and fast grid collision queries.

## 2. Studio Editor (`com.example.studio`)

- **Node Hierarchy (`SceneTreeDock`):** Tree visualization of scene objects with parent-child relationships and node creation shortcuts.
- **Inspector (`InspectorDock`):** Real-time node property modifications (Transform, Physics, Sprite, Particles, Behavior, Health, Rules).
- **Code Editor (`CodeEditorDock`):** GDScript text editor with syntax highlighting, script validation, and line numbers.
- **Visual Scripting (`VisualScriptDock`):** Signal-style Event/Action block builder suitable for non-programmers.
- **Debug Console (`DebugConsoleDock`):** Real-time output log stream, warning reporter, and error trace view.

## 3. Modular AI System (`com.example.studio.ai`)

- **`AiProvider` Interface:** Abstract contract for LLM completion requests (`generateText`, `generateScript`, `modifyScene`).
- **Provider Implementations:**
  - `OpenAiProvider`: OpenAI Chat Completions API (`gpt-4o`, `gpt-4o-mini`).
  - `GeminiProvider`: Google Gemini API (`gemini-1.5-pro`, `gemini-1.5-flash`).
  - `ClaudeProvider`: Anthropic Claude API (`claude-3-5-sonnet`).
  - `DeepSeekProvider`: DeepSeek API (`deepseek-chat`, `deepseek-coder`).
  - `LocalLlmProvider`: OpenAI-compatible local endpoints (Ollama, LM Studio, vLLM).
- **`AiGameAgent`:** High-level agent with full project context awareness. Translates developer prompts into scene updates, script generation, bug fixes, or prefab creation.

## 4. Android Build & APK Export (`com.example.studio.export`)

- **`ApkExporter`:** Generates packaged game bundles and standalone APK build configurations directly on the device.
