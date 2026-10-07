# Project Roadmap

## Milestone 1: Engine Core & Renderer (Completed)
- [x] Custom Canvas2D rendering pipeline & scale modes
- [x] Scene node graph & GameObject / Component system
- [x] 2D Physics Engine with gravity, rigid bodies, impulses, and AABB/Circle collisions
- [x] Audio system with procedural sound synthesizer

## Milestone 2: Android Studio Editor UI (Completed)
- [x] Touch-first viewport with pan, zoom, grid snapping
- [x] Node hierarchy & Inspector docks
- [x] Tilemap & Sprite Animation visual editors
- [x] GDScript/Lua live code editor
- [x] Asset Library & Template manager

## Milestone 3: Multi-Provider AI Agent Layer (Completed)
- [x] Provider-agnostic `AiAgentProvider` interface
- [x] OpenAI, Gemini, Claude, DeepSeek, and Local LLM provider adapters
- [x] `AiGameDevAgent` scene modification and script generation engine
- [x] `AiAssistantDock` Composable user interface

## Milestone 4: APK Export & Advanced Features (Completed)
- [x] In-app export packager dock for Android APK bundling
- [x] Comprehensive documentation (`README.md`, `ARCHITECTURE.md`, `ROADMAP.md`, `TODO.md`)
- [x] Continuous unit testing and build verification

## Future Enhancements
- [ ] Vulkan / OpenGL ES 3.0 hardware-accelerated batch renderer backend
- [ ] On-device Kotlin script compilation using embedded Kotlin compiler
- [ ] Multi-touch gesture editing (pinch-to-zoom in viewport)
