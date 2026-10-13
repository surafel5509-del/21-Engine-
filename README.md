# Android 2D Game Engine & Visual Studio

A touch-first, Godot-inspired, professional Android-first 2D Game Engine and Visual Game Studio built entirely with Kotlin and Jetpack Compose. Create, edit, script, preview, test, and export full 2D Android games directly on phone or tablet without requiring a PC.

## Key Features

- **Full Visual 2D Scene Editor**: Intuitive touch-first node tree management, interactive canvas viewports, multi-touch gizmos, and property inspectors.
- **Node & Scene System**: Hierarchical scene graph (`GameObject`, `Scene`, `Component`) supporting node parenting, transformations, prefabs, and scene switching.
- **Sprite & Texture Rendering**: Android Canvas2D hardware-accelerated batch renderer with support for render layers, custom z-indexing, sprite tinting, and blending.
- **Sprite Animation Editor**: Frame-by-frame animation, spritesheet slicing, clip playback controls, and keyframe timeline support.
- **Tilemap Editor**: Grid-based tile painter with layer management, collision boundary definitions, and custom tilesets.
- **2D Physics & Collision**: Custom lightweight 2D physics engine with rigidbodies, gravity, AABB/Circle colliders, impulse-based collision response, and spatial querying.
- **Particle System**: Configurable particle emitters for visual effects (burst, rate, color gradients, velocity ranges, lifetime, and particle scale).
- **Audio System**: Low-latency spatialized multi-channel audio playing background music (BGM) and sound effects (SFX) via Android SoundPool and MediaPlayer.
- **UI Designer & HUD**: Anchor-based dynamic UI layout system supporting buttons, panels, progress bars, labels, sliders, and virtual joysticks.
- **Camera System**: Dynamic 2D camera with target tracking, smooth follow smoothing, zoom control, viewport bounds clamping, and screen shaking.
- **Scripting & Visual Logic**: Multi-scripting runtime supporting custom Kotlin-like/GDScript-like dynamic components as well as node-based visual scripting graphs.
- **Pluggable AI Game Dev Agent**: Multi-provider AI assistant framework supporting OpenAI, Google Gemini, Anthropic Claude, DeepSeek, local LLMs, and offline heuristic fallbacks.
- **Project Manager & Asset Browser**: Full filesystem project manager to import images, audio files, JSON data, fonts, and manage project metadata.
- **Built-in Code Editor**: Code editor with dynamic syntax highlighting, auto-completion, and error reporting.
- **Direct APK Export**: In-app Gradle build wrapper and APK generator to package, sign, and export standalone Android games directly on-device.

## Project Structure

```
├── app/                      # Main Android Application module
│   └── src/main/java/com/example/
│       ├── engine/           # Modular Engine Core (runtime agnostic from editor)
│       │   ├── animation/    # Frame animation & timeline tweens
│       │   ├── assets/       # Asset management & file cache
│       │   ├── audio/        # Audio playback & spatial sound
│       │   ├── camera/       # 2D Camera system & screen shake
│       │   ├── components/   # ECS / Component definitions
│       │   ├── core/         # Game loop & engine lifecycle
│       │   ├── input/        # Touch gestures & virtual controls
│       │   ├── math/         # Vectors, transforms, collision math
│       │   ├── physics/      # Rigidbody2D & collision resolution
│       │   ├── rendering/    # Canvas2D renderer & render layers
│       │   ├── scene/        # Scene tree & GameObject hierarchy
│       │   ├── ui/           # Game UI elements & anchor layout
│       │   ├── utils/        # FPS counter & pooling
│       │   └── viewport/     # Viewport management & safe area insets
│       ├── studio/           # Visual Editor & UI Docks
│       │   ├── ai/           # Multi-provider AI Assistant Layer
│       │   ├── engine_bridge/# Studio runtime integration
│       │   ├── model/        # Project & node schemas
│       │   ├── storage/      # Custom asset & project storage
│       │   ├── templates/    # Project templates (Platformer, Physics Sandbox, Benchmark)
│       │   └── ui/           # Studio layout docks & screens
│       └── game/             # Built-in sample scenes & global HUD
├── ARCHITECTURE.md           # System design & architecture details
├── ROADMAP.md                # Development roadmap & milestones
└── TODO.md                   # Feature tracking & todo list
```

## Getting Started & Building

### Prerequisites
- JDK 17 or higher
- Android SDK 35 / Gradle 8.8
- Android Studio Ladybug or Gradle CLI

### Running Tests
To run unit tests locally:
```bash
gradle :app:testDebugUnitTest
```

### Building Debug APK
To build the debug APK:
```bash
gradle assembleDebug
```

## License
MIT License - Open-source Android-first 2D Game Engine and Studio.
