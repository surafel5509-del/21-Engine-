# Android 2D Game Engine & Studio (Godot-like)

A professional Android-first 2D game engine and visual game editor inspired by Godot workflow. Designed for full offline operation on mobile devices and tablets, complete with a modular, provider-agnostic AI Coding & Game Development Agent.

## Key Features

- **Visual 2D Scene Editor:** Touch-first viewport with move, zoom, grid snap, brush placement, and real-time node manipulation.
- **Scene & Node Hierarchy:** Node2D, CharacterBody2D, Sprite2D, AnimatedSprite2D, RigidBody2D, StaticBody2D, Area2D, CpuParticles2D, Camera2D, CanvasLayerUI, and Spawner2D.
- **Scripting System:** Built-in code editor supporting GDScript / Lua-like syntax with event hooks (`_ready`, `_process`), variable assignments, node properties, and visual script event-action triggers.
- **Tilemap Editor:** Visual grid tilemap system for multi-layer level design.
- **2D Physics Engine:** Rigid body physics, kinematic movement, custom AABB / Circle collision detection, impulses, and triggers.
- **Particles & FX:** Particle emitters with configurable birth, lifetime, gravity, angle spread, and color gradients.
- **Audio System:** Built-in synthesizer and audio clip manager for sound FX and background music.
- **Prefabs & Reusable Objects:** Save node subtrees as prefabs and instantiate them dynamically.
- **APK Export System:** Direct package and APK generator to build standalone Android executables on mobile.
- **Modular AI Agent:** Provider-agnostic AI Assistant supporting OpenAI, Google Gemini, Anthropic Claude, DeepSeek, and Local LLMs (Ollama / LocalAI). Can generate scripts, build scenes, fix errors, and modify project files.

## Project Architecture Overview

- `com.example.engine`: Core runtime game engine (Renderer, PhysicsWorld2D, SceneManager, GdScriptEngine, TilemapLayer, AudioManager, ParticleEmitterComponent, ViewportManager).
- `com.example.studio`: Godot-like visual studio UI (GodotStudioScreen, SceneTreeDock, InspectorDock, CodeEditorDock, AssetLibraryDock, VisualScriptDock, DebugConsoleDock).
- `com.example.studio.export`: Android APK build & export subsystem.
- `com.example.studio.ai`: Modular AI provider integration (`AiProvider`, `OpenAiProvider`, `GeminiProvider`, `ClaudeProvider`, `DeepSeekProvider`, `LocalLlmProvider`) and `AiGameAgent`.

## Getting Started

1. Open project in Android Studio or compile via Gradle.
2. Build debug APK: `./gradlew assembleDebug`
3. Run unit tests: `./gradlew :app:testDebugUnitTest`
