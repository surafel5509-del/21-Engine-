# Android 2D Game Engine & Studio

An Android-first 2D game engine and visual editor inspired by Godot's workflow, designed to let game developers create, test, and export complete 2D games directly on phone or tablet without needing a PC.

## Key Features

- **Full Visual 2D Scene Editor**: Mobile-optimized scene canvas, camera navigation, selection gizmos, node dragging, and inspector panel.
- **Node & Scene Tree System**: Modular scene hierarchy with transform propagation, component composition, and node management.
- **Sprite & Texture System**: Support for sprite rendering, animated sprite sheets, and asset import.
- **Asset Manager & Storage**: Local storage manager for images, audio, scripts, fonts, and scene prefabs.
- **2D Physics Engine**: RigidBodies, colliders (Box, Circle), gravity, impulses, and collision callback resolution.
- **Particle System**: Customizable particle emitters with speed, spread, gravity, color gradients, and lifetime.
- **Audio System**: Sound effect playback and ambient music management using Android SoundPool & MediaPlayer.
- **UI Designer Engine**: Built-in 2D UI layout nodes (`UIButton`, `UILabel`, `UIProgressBar`, `UISlider`, `UICanvas`).
- **Scripting & Visual Logic**: Custom Kotlin/GDScript-like script component execution and node-based visual scripting graphs.
- **Pluggable AI Assistant**: Built-in AI coding/game-dev agent supporting OpenAI, Gemini, Claude, DeepSeek, local LLMs, and offline fallback provider.
- **APK Export Engine**: Direct APK building and export flow on Android.

## Getting Started

### Requirements
- Android SDK 35 (Android 7.0 / API 24 minimum)
- JDK 17
- Gradle 8.8

### Building and Running Unit Tests
```bash
# Run unit test suite
gradle :app:testDebugUnitTest

# Build debug APK
gradle assembleDebug
```

## Project Structure
- `app/src/main/java/com/example/engine/`: Core game runtime engine independent of the editor (Renderer, Physics, Camera, Audio, Input, Scene, Components).
- `app/src/main/java/com/example/studio/`: Visual Editor, Inspector, Docks, Project Storage, and Engine Bridges.
- `app/src/main/java/com/example/studio/ai/`: AI Assistant layer with pluggable `AiProvider` interface and multi-provider implementations.

## Documentation
- [ARCHITECTURE.md](ARCHITECTURE.md) - Deep dive into architecture and modular design.
- [ROADMAP.md](ROADMAP.md) - Milestone roadmap and development progress.
- [TODO.md](TODO.md) - Immediate and upcoming task backlog.
