# Godot Studio 2D - Android 2D Game Engine & Studio

A professional, touch-first Android-first 2D Game Engine and Visual Game Studio inspired by Godot workflow. Build, edit, script, test, and export complete 2D games directly on your Android phone or tablet without needing a PC.

---

## Key Features

- **Full Visual 2D Scene Editor**: Real-time canvas editing with grid snap, viewport pan/zoom, selection gizmos, brush placement, and multi-node inspection.
- **Node & Scene Hierarchy System**: Godot-style node hierarchy supporting `Node2D`, `CharacterBody2D`, `Sprite2D`, `AnimatedSprite2D`, `TileMap2D`, `RigidBody2D`, `StaticBody2D`, `Area2D`, `CPUParticles2D`, `Camera2D`, `CanvasLayerUI`, and `Spawner2D`.
- **Pluggable AI Copilot Agent**: Pluggable AI assistant supporting OpenAI, Google Gemini, Anthropic Claude, DeepSeek, OpenAI-compatible local endpoints (Ollama, LM Studio), and an **Offline Local Heuristic Engine**.
- **Dual Scripting Engine**:
  - **GDScript / Lua-like Text Code Editor**: Full-featured code editor tab (`Code.gd`) with code templates (`_ready()`, `_process(delta)`).
  - **Visual Scripting Blocks**: Node-based logic rules connecting events to actions (`ON_COLLISION_ENTER -> PLAY_SOUND & ADD_SCORE`).
- **2D Physics Engine**: Rigid bodies, box/circle collision detection, impulses, bounciness, friction, gravity customization, triggers, and one-way platforms.
- **Particle & FX System**: Real-time CPU particle emitter with custom speed, angle spread, lifetime, color gradients, and emission rates.
- **Audio System**: Built-in sound effect synthesizer & audio stream manager for jump, laser, coin, hit, explosion, and powerup sounds.
- **Tilemap Editor**: Interactive tile grid creation with custom tile sizes, palette brushes, and auto-collision.
- **Standalone APK Export**: Package and export standalone Android APK project bundles directly from your device.
- **Debug Console & Metrics**: Real-time logging, FPS counter, draw call analytics, memory metrics, and "Ask AI to Fix Error" integration.
- **Touch-First Dark Modern UI**: Built with Jetpack Compose for phone and tablet touch displays.

---

## Getting Started

### Requirements
- Android 7.0+ (API Level 24+)
- Gradle 8.8+ & Kotlin 2.0+

### Build & Run
```bash
# Run unit tests
gradle testDebugUnitTest

# Compile debug APK
gradle assembleDebug
```

---

## License
Original design and codebase for Godot Studio 2D.
