# GodotStudio Architecture Specification

## Overview

GodotStudio is structured into two primary layers:
1. **Engine Core (`com.example.engine`)**: Independent runtime engine responsible for math, scene trees, rendering, physics, audio, particle simulation, and scripting callbacks.
2. **Editor & Studio (`com.example.studio`)**: Touch-optimized mobile IDE containing visual viewport editing, inspector docks, code editor, AI development agent, project storage, and APK packaging.

---

## Detailed Component Architecture

### 1. Engine Core (`com.example.engine`)

- **`core/`**: `GameEngine`, `GameLoop`, `EngineConfig`, `Time`. Handles target frame rate, delta updates, lifecycle loop, and surface attachment.
- **`math/`**: `Vector2`, `Transform2D`, `Rect2D`, `Circle2D`, `Ray2D`, `MathUtils`. High-performance 2D vector and geometry utilities.
- **`scene/`**: `Scene`, `GameObject`, `Component`, `SceneManager`. Tree hierarchy with parent-child transforms, lifecycle callbacks (`onStart`, `onUpdate`, `onDestroy`), and scene transitions.
- **`rendering/`**: `Canvas2DRenderer`, `RenderLayer`, `Sprite`. Hardware-accelerated canvas rendering with layer depth sorting.
- **`physics/`**: `PhysicsWorld2D`, `RigidBody2DComponent`, `Collider2DComponent`. AABB and circle collision resolution, gravity vectors, rest friction, and impulse physics.
- **`animation/`**: `SpriteSheet`, `AnimationClip`, `Tween`. Sprite frame sequence playback and parametric easing tweens.
- **`audio/`**: `AudioManager`. Built-in synthesizer for sound effects (jump, laser, explosion, coin pickup) and audio file playback.
- **`input/`**: `InputManager`, `VirtualJoystick`, `VirtualButton`, `GestureDetector2D`. Touch events, multi-finger input tracking, and virtual controllers.
- **`ui/`**: `UICanvas`, `UIElement`, `UIButton`, `UILabel`, `UIPanel`, `UIProgressBar`, `UIJoystick`. Anchor-based layout engine for game UIs.
- **`components/`**: `SpriteRendererComponent`, `AnimatedSpriteComponent`, `ParticleEmitterComponent`, `ScriptComponent`, `ShapeRendererComponent`, `TextRendererComponent`. Attachable capabilities for game objects.

---

### 2. Studio & Visual Editor (`com.example.studio`)

- **`model/`**: `GameProject`, `GameNode`, `NodeType`, `VisualScriptRule`. Data model representing the full project state, scene graph, and script rules.
- **`storage/`**: `ProjectStorageManager`, `CustomAssetManager`. Local JSON persistence and asset asset store.
- **`engine_bridge/`**: `StudioRuntimeScene`, `ShapeDrawer`. Translates visual `GameNode` hierarchy into runtime `Scene` and handles editor gizmos/touch selection.
- **`ui/`**:
  - `GodotStudioScreen.kt`: Main Android Jetpack Compose layout housing Viewport, Top Bar, and Dock Panels.
  - `StudioTopAppBar.kt`: Play/Pause controls, grid snap toggle, save, and APK export button.
  - `SceneTreeDock.kt`: Tree view hierarchy for adding, reordering, duplicating, and deleting nodes.
  - `InspectorDock.kt`: Dynamic property editor for position, scale, rotation, physics, particles, and custom scripts.
  - `AssetLibraryDock.kt`: Texture, sound FX, and tilemap asset browser.
  - `VisualScriptDock.kt`: Visual event-action flow rules and GDScript/Kotlin code preview.
  - `CodeEditorDock.kt`: Built-in script code editor with syntax highlighting and AI code fixing shortcuts.
  - `DebugConsoleDock.kt`: Live logging, frame time diagnostics, and one-tap AI error analysis.
  - `ApkExportModal.kt`: Standalone APK configuration and build launcher.

---

### 3. AI Agent Architecture (`com.example.studio.ai`)

- **Provider Abstraction (`AiProvider`)**:
  - `OpenAiProvider`: OpenAI Chat Completions API.
  - `GeminiProvider`: Google Gemini REST API.
  - `ClaudeProvider`: Anthropic Claude Messages API.
  - `DeepSeekProvider`: DeepSeek Chat API.
  - `OpenAiCompatibleProvider`: Generic custom OpenAI endpoint.
  - `LocalLlmProvider`: Local HTTP / Ollama server interface.
- **Agent Action Parser & Engine (`AiAgentEngine`)**:
  - Evaluates user natural language instructions into structured actions (`AiAgentAction`):
    - `CREATE_NODE`, `MODIFY_NODE`, `GENERATE_SCRIPT`, `EXPLAIN_ERROR`, `FIX_CODE`, `SEARCH_PROJECT`, `EXPORT_PROJECT`.
  - Executes updates directly on the active `GameProject` or scene tree.

---

## Data Flow Diagram

```
[ User Touch Input / AI Prompt ]
         │
         ▼
[ Editor UI / AI Agent Engine ]
         │
         ▼
[ GameProject & GameNode Model ]
         │
         ▼
[ StudioRuntimeScene Bridge ]
         │
         ▼
[ Engine Core Runtime Loop (Physics -> Scripting -> Renderer -> Surface) ]
```
