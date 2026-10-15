# Development Roadmap

## Milestone 1: Engine Core & Architecture Foundation (Completed)
- [x] Establish core engine packages (`engine.core`, `engine.scene`, `engine.rendering`, `engine.physics`, `engine.audio`, `engine.input`, `engine.ui`).
- [x] Create Gradle setup and build fix for AGP 8.5.2 & compileSdk 35.
- [x] Implement unit testing infrastructure for core math, scene graph, and physics.
- [x] Initial architecture documentation (`README.md`, `ARCHITECTURE.md`, `ROADMAP.md`, `TODO.md`).

## Milestone 2: Visual Editor & Touch Tools
- [x] Godot-inspired visual screen layout with dark theme (`GodotStudioScreen`).
- [x] Node hierarchy tree dock (`SceneTreeDock`).
- [x] Property inspector dock (`InspectorDock`).
- [x] Canvas viewport with gesture panning and zooming.

## Milestone 3: Asset System & Project Persistence
- [x] Project storage manager for scene saving/loading (`ProjectStorageManager`).
- [x] Asset library dock (`AssetLibraryDock`).
- [ ] Asset importer for custom audio, fonts, and sprite atlases.

## Milestone 4: Scripting & Logic Systems
- [x] Script components (`ScriptComponent`).
- [x] Visual scripting dock (`VisualScriptDock`).
- [ ] Embedded GDScript/Lua-like interpreter runtime.

## Milestone 5: Advanced Subsystems (Physics, Animation, Audio, UI)
- [x] RigidBody2D and Colliders (`RigidBody2DComponent`, `Collider2DComponent`).
- [x] Particle Emitters (`ParticleEmitterComponent`).
- [x] Sprite Sheet Animation (`AnimatedSpriteComponent`).
- [x] In-game UI engine (`UICanvas`, `UIButton`, `UILabel`, `UISlider`).

## Milestone 6: AI Assistant Layer
- [x] Pluggable `AiProvider` architecture contract.
- [x] Providers for OpenAI, Gemini, Claude, DeepSeek, Local LLM, and Offline Heuristics.
- [ ] AI prompt actions for direct scene tree manipulation and auto-script generation.

## Milestone 7: APK Export & On-Device Packaging
- [x] Export manager stub for standalone APK creation.
- [ ] Integrated AAPT2 / D8 pipeline for on-device standalone APK compilation.
