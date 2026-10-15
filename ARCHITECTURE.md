# System Architecture

The Android 2D Game Engine and Studio is structured into strictly separated modules: **Engine Core**, **Editor UI & Studio Tools**, and **AI Agent Layer**.

```
+-----------------------------------------------------------------------+
|                              Editor UI                                 |
|  (GodotStudioScreen, SceneTreeDock, InspectorDock, AssetLibrary, etc) |
+-----------------------------------+-----------------------------------+
                                    |
                                    v
+-----------------------------------------------------------------------+
|                             Studio Bridge                             |
|              (StudioRuntimeScene, ProjectStorageManager)              |
+-----------------------------------+-----------------------------------+
                                    |
                                    v
+-----------------------------------------------------------------------+
|                              Engine Core                              |
|  (GameEngine, SceneManager, PhysicsWorld2D, Canvas2DRenderer, etc)    |
+-----------------------------------+-----------------------------------+
                                    |
                                    v
+-----------------------------------------------------------------------+
|                            AI Agent Layer                             |
|      (AiAgent, AiProvider Interface -> OpenAI / Gemini / Local)      |
+-----------------------------------------------------------------------+
```

## 1. Engine Core (`com.example.engine`)

The engine core is completely decoupled from Jetpack Compose UI editor elements and manages runtime simulation:

- **Core & Game Loop (`com.example.engine.core`)**: `GameEngine`, `GameLoop`, `EngineConfig`, `Time`. Handles variable update rate, fixed physics updates, frame rendering ticks, and scene lifecycle.
- **Scene & GameObject (`com.example.engine.scene`)**:
  - `Scene`: Container for active `GameObject` hierarchy.
  - `GameObject`: Node with transform (`Transform2D`), tag, layer, parent/children references, and `Component` list.
  - `Component`: Lifecycle contracts (`onStart`, `onUpdate`, `onFixedUpdate`, `onRender`, `onDestroy`).
- **Rendering (`com.example.engine.rendering`)**: `Canvas2DRenderer`, `Camera2D`, `RenderLayer`, sorting by Z-index/layer, hardware-accelerated Android Canvas rendering.
- **Physics (`com.example.engine.physics`)**: Custom 2D rigid body physics, collision detection (AABB, Circle, Raycasts), impulse resolution, and trigger callbacks.
- **Input (`com.example.engine.input`)**: Multi-touch handling, `GestureDetector2D`, virtual joysticks, virtual buttons, mouse/keyboard integration.
- **Audio (`com.example.engine.audio`)**: Sound effect pool (`SoundPool`) and background audio streams (`MediaPlayer`).
- **UI System (`com.example.engine.ui`)**: Anchor-based layout hierarchy (`UILabel`, `UIButton`, `UIProgressBar`, `UISlider`, `UICanvas`).

## 2. Visual Editor & Studio (`com.example.studio`)

The Studio layer provides a touch-first, dark modern IDE built using Jetpack Compose:

- **GodotStudioScreen**: Main layout with top app bar, scene viewport, retractable side docks (Scene Tree, Inspector, Asset Library, Visual Scripting, Templates, AI Assistant).
- **SceneTreeDock**: Tree view showing parent-child node relationships, visibility toggles, and add/remove node controls.
- **InspectorDock**: Dynamic property editor allowing real-time modification of position, rotation, scale, color, physics properties, and attached scripts.
- **ProjectStorageManager**: Persists `GameProject` configurations, scene files, assets, and prefabs in JSON format to local storage.

## 3. Pluggable AI Agent Layer (`com.example.studio.ai` / engine architecture)

The AI layer exposes an extensible provider interface:

```kotlin
interface AiProvider {
    val id: String
    val displayName: String
    suspend fun generateResponse(prompt: String, context: AiContext): AiResult
}
```

Implementations include:
- `OpenAiProvider`: OpenAI Chat Completions API integration.
- `GeminiProvider`: Google Gemini AI API integration.
- `ClaudeProvider`: Anthropic Claude API integration.
- `DeepSeekProvider`: DeepSeek API integration.
- `LocalLlmProvider`: OpenAI-compatible local HTTP endpoint (e.g., Ollama / LM Studio).
- `OfflineHeuristicProvider`: Built-in offline fallback that handles scene creation, script generation, and bug fixing without external internet access.
