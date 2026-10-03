# Apex2D Game Studio & Engine

An Android-first 2D Game Engine and Visual Game Studio inspired by Godot workflow, built from scratch in Kotlin and Jetpack Compose.

Apex2D enables game creators to build, edit, script, test, and export complete 2D games directly on mobile devices (phones & tablets) or with touch, keyboard, and mouse support.

---

## Key Features

- **Visual 2D Scene Editor**: Real-time viewport rendering with pan, pinch-zoom, selection gizmos, placement brushes, and grid snapping.
- **Node & Scene System**: Node-based hierarchy (`Node2D`, `Sprite2D`, `AnimatedSprite2D`, `RigidBody2D`, `StaticBody2D`, `Area2D`, `CPUParticles2D`, `Camera2D`, `TileMap2D`, `CanvasLayerUI`).
- **Asset Manager**: Built-in asset manager supporting custom textures, procedurally generated sprites, audio effects, tilemaps, fonts, and prefabs.
- **Scripting & Visual Scripting**: GDScript / Lua-style code editor with syntax highlighting, alongside a node-based Visual Event/Action Scripting flow.
- **2D Physics Engine**: RigidBody2D, StaticBody2D, Area triggers, circle/box colliders, impulse application, gravity scaling, bounciness, friction, and collision callbacks.
- **Tilemap Editor**: Interactive tile palette picker, paint/erase tools, and grid layer collision setup.
- **Particle System**: CPU particle emitters with configurable emission rate, velocity, lifetime, start/end colors, sizes, and gravity.
- **Audio Synthesizer & Player**: Pure Kotlin audio synthesis engine producing sound effects (jump, laser, explosion, coin pickup, powerup, hurt) and audio playback.
- **Debug Console**: Embedded log viewer with filtering, warnings, error reporting, stack trace views, and clear log options.
- **AI Game Development Assistant**: Modular AI agent supporting OpenAI, Google Gemini, Anthropic Claude, DeepSeek, OpenAI-Compatible APIs, and Local LLMs.
- **APK Export & Project Management**: Export projects to JSON / runnable APK packages directly from Android.

---

## Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or Gradle 8.8+
- JDK 17+ or JDK 21
- Android Device or Emulator running Android 7.0 (API level 24) or higher

### Building & Running
```bash
# Run unit tests
gradle test

# Build debug APK
gradle assembleDebug
```

---

## AI Agent Setup

1. Open the **AI Assistant** tab in the Studio Dock.
2. Click **Provider Settings** to configure your preferred LLM provider:
   - Google Gemini
   - OpenAI
   - Anthropic Claude
   - DeepSeek
   - Custom / Local LLM (Ollama, LM Studio)
3. Enter your API Key and endpoint (if applicable).
4. Use prompt actions or chat directly to create scenes, modify nodes, generate scripts, or fix code errors.
