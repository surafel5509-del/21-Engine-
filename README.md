# Godot-Inspired 2D Game Studio for Android

An Android-first 2D game engine and visual game development studio built with Kotlin and Jetpack Compose. Inspired by the node-based workflow and rapid iteration of Godot Engine, this application empowers developers to build, edit, script, test, and run 2D games directly on mobile devices and tablets without requiring a desktop PC.

---

## Key Features

### Engine Core
- **Scene & Node Hierarchy System:** Tree-structured scene graph supporting 2D transforms, parent-child inheritance, and prefab instantiation.
- **Canvas2D & Hardware-Accelerated Renderer:** Supports sprites, animated sprite sheets, custom geometric primitives, multi-layer rendering, camera viewports, and custom particle systems.
- **2D Physics Engine:** RigidBody2D & StaticBody2D physics with continuous collision detection, gravity, restitution, friction, and trigger colliders.
- **Audio System:** Real-time procedural audio synthesizer and sound effect playback for offline gameplay without external media dependencies.
- **Touch & Input System:** Built-in multi-touch gesture detectors, touch joystick UI controls, and mouse/keyboard support.
- **UI System:** In-engine resolution-independent canvas UI (Buttons, Sliders, Progress Bars, Labels, Panels).

### Mobile Game Studio (Visual Editor)
- **Visual 2D Scene Viewport:** Real-time editing with pan, zoom, grid snapping, tool selection (Select/Move, Quick Brush placement).
- **Scene Tree Hierarchy Dock:** Inspect, add, duplicate, reorder, or delete game nodes.
- **Inspector Dock:** Real-time parameter tweak for node positions, scale, color, physics properties, collision bounds, and script triggers.
- **Visual Scripting & Logic Editor:** Visual node-based visual script trigger configuration (On Touch, On Collision, Auto Move, Bounce, Spawner).
- **Asset Library & FX Manager:** Browse, import, custom asset assignments, sprite sheet slice configurations, and built-in procedural FX preview.
- **Templates Dock:** Pre-configured game templates (2D Platformer, Physics Sandbox, Stress Benchmark) for instant editing and learning.
- **Instant Live Play Testing:** One-tap toggle between editor view and runtime engine play mode with live restart and debug HUD.

### Modular AI Assistant Layer
- **Provider-Agnostic AI Agent:** Built-in coding and scene-creation AI assistant. Connects seamlessly to multiple AI providers without hardcoding:
  - **Firebase Gemini:** Powered by `firebase-ai` SDK.
  - **OpenAI-Compatible REST API:** Supports OpenAI (GPT-4o), Anthropic Claude, DeepSeek, and Local LLM server endpoints (Ollama, vLLM, LM Studio).
  - **Offline Rule-Based Local AI:** Fully functional offline AI assistant running locally without network or API key requirements.
- **AI Capabilities:**
  - Create and modify scene nodes dynamically.
  - Generate and attach visual script logic.
  - Explain runtime errors and fix script logic.
  - Search project tree and suggest game assets / templates.

---

## Project Structure

```
├── app/
│   ├── src/main/java/com/example/
│   │   ├── engine/                # Runtime 2D Game Engine (Independent of Editor)
│   │   │   ├── animation/         # Sprite sheets, animation clips, tweens
│   │   │   ├── assets/            # Engine asset loader & cache
│   │   │   ├── audio/             # Procedural audio synthesizer & audio manager
│   │   │   ├── camera/            # 2D camera viewport & transform matrices
│   │   │   ├── components/        # ECS component implementations
│   │   │   ├── core/              # Game engine loop, surface view, time & config
│   │   │   ├── input/             # Multi-touch input, virtual joystick, gestures
│   │   │   ├── math/              # Vector2, Rect2D, Circle2D, Ray2D, Transform2D
│   │   │   ├── physics/           # 2D physics world & collision solvers
│   │   │   ├── rendering/         # Canvas2D renderer & render layers
│   │   │   ├── scene/             # Scene manager, GameObject & Scene graphs
│   │   │   ├── ui/                # In-engine UI components
│   │   │   └── viewport/          # Viewport scaling & safe area insets
│   │   │
│   │   ├── studio/                # Visual Studio & Android Editor UI
│   │   │   ├── ai/                # Provider-agnostic AI Coding/Game Agent Layer
│   │   │   ├── engine_bridge/     # Editor-to-Engine runtime bridge & shape drawers
│   │   │   ├── model/             # GameProject & GameNode models
│   │   │   ├── storage/           # Local project storage & asset persistence
│   │   │   ├── templates/         # Game project templates
│   │   │   └── ui/                # Studio Compose UI (Inspector, Hierarchy, AI, Scripting)
│   │   │
│   │   ├── game/                  # Prebuilt demo scenes and HUD overlay
│   │   └── MainActivity.kt        # Entry point for Android application
│   └── src/test/                  # Unit and Roborazzi snapshot tests
├── ARCHITECTURE.md                # System Architecture Documentation
├── ROADMAP.md                     # Development Milestone Roadmap
└── TODO.md                        # Task Tracking List
```

---

## Building and Running

### Prerequisites
- JDK 17+
- Android SDK with target SDK 35
- Gradle 8.8+

### Build Commands

Run unit tests:
```bash
gradle testDebugUnitTest
```

Build debug APK:
```bash
gradle assembleDebug
```

The compiled APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.
