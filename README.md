# Android 2D Game Engine & Godot Studio

An Android-first 2D Game Engine and Visual Editor inspired by the workflow of Godot, built with Kotlin and Jetpack Compose.

## Features

- **2D Visual Scene Editor**: Touch-first 2D viewport with grid snapping, camera zoom/pan controls, and visual selection handle manipulation.
- **Node Hierarchy & Scene Tree**: Node graph system supporting Node2D, Sprite2D, AnimatedSprite2D, RigidBody2D, StaticBody2D, Area2D, Camera2D, CpuParticles2D, TileMap, CanvasLayer, Control, SoundPlayer2D.
- **Physics & Collision Engine**: Custom 2D rigid body physics simulation, gravity, restitution, friction, bounding box / circle collision detection, impulse solvers, and spatial queries.
- **Tilemap & Sprite Animation Editors**: Built-in palette tile painter and frame keyframe / grid sprite animation editor.
- **Code & Visual Scripting Engine**: Built-in GDScript/Lua-like scripting code editor and visual node-based script block builder.
- **Asset Manager & Synthesizer**: Asset library manager supporting custom dynamic dynamic procedural sprite generation and retro audio synthesizer FX.
- **Standalone APK Export System**: Integrated export settings tab for building standalone Android APK packages directly from phone or tablet.
- **Multi-Provider AI Game Dev Assistant**: Built-in AI coding agent supporting OpenAI, Google Gemini, Anthropic Claude, DeepSeek, and Local LLM (Ollama) endpoints to generate nodes, write scripts, fix errors, and modify scene properties.
- **Offline Operation**: Core runtime engine and editor operate fully offline.

## System Architecture

```
App Architecture
├── Engine Core (`com.example.engine`)
│   ├── Core & Game Loop (GameEngine, GameSurfaceView, Time)
│   ├── Math & Transforms (Vector2, Transform2D, Rect2D, Circle2D)
│   ├── Physics & Collision (PhysicsWorld2D, RigidBody, Collider)
│   ├── Scene Graph & Components (Scene, GameObject, Component)
│   ├── Camera & Viewport (Camera2D, ViewportManager)
│   ├── Audio System (AudioManager, Synth)
│   ├── UI Framework (UICanvas, UIButton, UIPanel, UISlider)
│   └── Input Management (InputManager, VirtualJoystick, TouchEvent)
└── Studio Editor (`com.example.studio`)
    ├── UI Docks (SceneTreeDock, InspectorDock, AssetLibraryDock, TilemapDock, CodeEditorDock, ExportProjectDock)
    ├── AI Agent Framework (`com.example.studio.ai`: AiAgentProvider, AiGameDevAgent, AiAssistantDock)
    ├── Engine Bridge & Runtime Integration (StudioRuntimeScene)
    └── Storage & Templates (ProjectStorageManager, CustomAssetManager, ProjectTemplates)
```

## Running & Testing

To build the debug APK:
```bash
gradle assembleDebug
```

To run unit tests:
```bash
gradle testDebugUnitTest
```
