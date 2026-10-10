# 2D Game Engine & Studio for Android

An Android-first 2D game engine and visual game editor inspired by Godot workflow, built completely in Kotlin and Jetpack Compose. Users can design, script, test, build, and export complete 2D games directly on phone or tablet devices.

---

## Key Features

- **Full Visual 2D Scene Editor**: Touch-first 2D viewport with grid snapping, zooming, panning, drag-and-drop object placement, and visual transformation handles.
- **Node & Scene Hierarchy System**: Scene tree hierarchy supporting 2D Nodes, CharacterBody2D, RigidBody2D, StaticBody2D, Area2D, Tilemap, CPU Particles2D, Camera2D, Parallax Backgrounds, and CanvasLayer UI elements.
- **Built-In Code & Visual Scripting Engine**:
  - GDScript / Lua-like lightweight scripting engine with lifecycle hooks (`_ready`, `_process`), node property access, movement, physics impulses, and audio triggers.
  - Visual Scripting rule engine for no-code/low-code game creation.
  - Built-in Android code editor dock with syntax highlighting, script templates, line numbers, and live linting.
- **Tilemap Editor & Palette Manager**: Visual tile painter with tile selection, grid overlays, multi-layer rendering, and custom collision layers.
- **2D Physics Engine**: Collision detection (Box & Circle), impulse application, bounciness, friction, static/kinematic/dynamic body types, and triggers.
- **Sprite & Animation System**: Sprite sheet animator with customizable frame rates, loop controls, column/row configuration, and color tints.
- **Particle System**: Highly customizable CPU particle emitter supporting velocity spread, color gradients, size scaling, and lifetime curves.
- **Audio System**: Asset audio playback combined with built-in procedural Web/Synthesizer SFX generator (Jump, Coin, Laser, Explosion, Hit).
- **UI Designer**: Anchor-aware UI elements including UILabel, UIButton, UIProgressBar, UIPanel, and UISlider.
- **Debug Console & Diagnostics**: Live log output, runtime error reporting, FPS counter, and collision box debugging.
- **Project Manager & Direct APK Build**: Local project persistence, JSON import/export, project templates (Platformer, Top-Down RPG, Physics Sandbox, Flappy Bird), and standalone Android APK export tools.
- **Pluggable AI Coding & Game-Dev Copilot**:
  - Built-in AI coding agent that can search projects, create/modify scenes, write GDScript code, fix errors, create prefabs, and test builds.
  - Supports multiple AI providers: OpenAI, Google Gemini, Anthropic Claude, DeepSeek, OpenAI-Compatible APIs, and an Offline Local Heuristic AI.

---

## Architecture Blueprint

```
+-------------------------------------------------------------------+
|                        Godot Studio UI                            |
| (Compose Docks: Hierarchy, Inspector, Assets, Tilemap, Script,    |
|  Debug Console, Project Manager, AI Copilot)                      |
+-------------------------------------------------------------------+
                                 |
                                 v
+-------------------------------------------------------------------+
|                           AI Layer                                |
|  (AiAgent, AiProvider: OpenAI, Gemini, Claude, DeepSeek, Local)   |
+-------------------------------------------------------------------+
                                 |
                                 v
+-------------------------------------------------------------------+
|                          Engine Core                              |
|  - Renderer (Canvas2D)               - Physics (PhysicsWorld2D)   |
|  - Scene System (Node2D, SceneTree)  - Scripting (ScriptEngine)   |
|  - Tilemap & Sprite System           - Audio & Asset Manager      |
+-------------------------------------------------------------------+
```

---

## Building and Testing

### Prerequisites
- JDK 17
- Android SDK (API 35 / 36)
- Gradle 8.8

### Command Line
```bash
# Run Unit Tests
gradle testDebugUnitTest

# Compile Debug APK
gradle assembleDebug
```

---

## License

Original design and implementation under MIT License.
