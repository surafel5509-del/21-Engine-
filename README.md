# Android 2D Game Engine & Visual Studio

A self-contained, touch-first Android 2D Game Engine and Visual Editor inspired by Godot. Build, test, and export complete 2D games directly on Android mobile devices and tablets without needing a PC.

## Key Features

- **Visual Scene Editor**: Touch-first node hierarchy manager, live surface viewport with zoom, pan, grid-snapping, and drag-and-drop node placement.
- **Node & Scene Hierarchy**: Godot-like node tree supporting `Node2D`, `Sprite2D`, `AnimatedSprite2D`, `RigidBody2D`, `StaticBody2D`, `Area2D`, `Camera2D`, `CPUParticles2D`, `TileMap`, `ControlUI`, and sound nodes.
- **Built-in Scripting Engine**: Lightweight GDScript/Lua-like runtime interpreter supporting event-driven updates (`_ready`, `_process`, `_on_body_entered`), variables, and math routines.
- **Visual & Code Editor**: Integrated multi-tab code editor with line numbers, error diagnostic reporting, and auto-completion.
- **Tilemap Editor**: Built-in tile palette painter with draw, erase, and fill tools.
- **2D Physics Engine**: SAT (Separating Axis Theorem) and AABB collision detection, impulse-based dynamics, restitution, friction, and collision layers.
- **Particle & Sprite Animation**: Customizable CPU particle emitters (velocity, spread, lifetime, color gradient) and sprite sheet animation controller.
- **Integrated Audio System**: Real-time synthesized audio generator and multi-channel sound manager.
- **Standalone APK Export**: Export mobile games directly into installable APK packages or self-contained project archives.
- **Pluggable AI Assistant**: Built-in coding and scene agent supporting OpenAI, Google Gemini, Anthropic Claude, DeepSeek, custom OpenAI-compatible endpoints, and an offline local rule-based heuristic model.

## Quick Start & Usage

1. Open the project in Android Studio or compile with `./gradlew assembleDebug`.
2. Launch the app on an Android device or emulator (Android 7.0 / API 24+).
3. Choose a template or create a blank project.
4. Use the **Hierarchy Dock** to add nodes, the **Inspector Dock** to tweak properties, and the **Tilemap Dock** to paint levels.
5. Write game logic in the **Script Dock** or ask the **AI Assistant** to write scripts or fix errors.
6. Press **PLAY** to preview live game physics and controls.
7. Use the **Export Dock** to build and share your APK.
