# GodotStudio 2D Game Engine & Mobile Visual Editor

**GodotStudio** is an Android-first 2D game engine and mobile visual studio inspired by Godot's node-based workflow. It enables game developers, indie creators, and enthusiasts to build, code, test, and export complete 2D games directly on mobile devices without needing a desktop PC.

---

## Key Features

- **Touch-First 2D Visual Editor**: Real-time canvas editing, multi-touch panning & zooming, grid snapping, and brush tools.
- **Node & Scene Tree Architecture**: Godot-style hierarchical scene composition (Nodes, Sprites, Tilemaps, Cameras, RigidBodies, Particles, UI Elements).
- **Multi-Provider AI Assistant**: Modular AI coding agent integration supporting OpenAI, Google Gemini, Anthropic Claude, DeepSeek, OpenAI-compatible APIs, and Local LLMs (Ollama).
- **Built-In Code & Visual Editor**: Integrated GDScript / Lua / Kotlin syntax code editor with auto-completion, line numbers, and visual event-action blocks.
- **Physics & Particle Engine**: Custom 2D physics engine with rigid bodies, gravity, collision listeners, and customizable CPU particle systems.
- **Audio & Asset Manager**: Built-in sound synthesizer, audio playback, texture importer, and tilemap atlas generator.
- **Real-Time Game Runtime & Debug Console**: Single-tap game execution with live FPS counters, draw call stats, error reporting, and AI error analysis.
- **Standalone APK Export**: Build and export signed game APK packages directly on Android devices.

---

## Modular System Architecture

```
Engine Core
 ├── Renderer (Canvas 2D, Draw Calls, Layers)
 ├── Scene System (Node Hierarchy, Components, Lifecycles)
 ├── Asset System (Textures, Sound FX, Fonts, Tilemaps)
 ├── Physics Engine (Collision Detection, Forces, Velocity, RigidBodies)
 ├── Animation System (Sprite Sheets, Keyframes, Tweens)
 ├── Audio Manager (Synth, SFX, BGM)
 ├── UI Designer (Buttons, Labels, Panels, Joysticks)
 └── Scripting System (Visual Logic, GDScript Syntax, Event Callbacks)

Studio Editor
 ├── GodotStudio UI (Touch Controls, Viewport, Dock Panels)
 ├── Scene Tree & Inspector Docks
 ├── Asset & Template Library Docks
 ├── Built-in Code Editor & Script Runner
 ├── Real-time Debug Console
 ├── Direct APK Build & Export Pipeline
 └── AI Development Agent (Multi-Provider LLM Integration)
```

---

## Getting Started & Building

### Prerequisites
- JDK 17+
- Android SDK (API Level 35)
- Gradle 8.8+

### Build & Run Tests

```bash
# Run Unit and Robolectric Tests
gradle testDebugUnitTest

# Compile Debug APK
gradle assembleDebug
```

---

## License

Created as an open-source Android 2D game engine and visual development studio.
