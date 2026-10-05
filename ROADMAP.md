# GodotStudio Development Roadmap

## Milestone 1: Core Engine Architecture & Prototype ✅
- Modular 2D engine runtime (`GameEngine`, `GameLoop`, `Canvas2DRenderer`)
- Scene tree hierarchy (`GameObject`, `Scene`, `Component`)
- Math primitives and viewport scaling (Fit Letterbox, Stretch)

## Milestone 2: Android Visual Editor Shell ✅
- Jetpack Compose mobile editor layout
- Viewport interactive touch controls (Pan, Zoom, Select, Place)
- Scene Tree dock and Inspector property editor

## Milestone 3: Asset & Physics System ✅
- Custom asset manager (textures, synthesizer sounds, sprite sheets)
- 2D Physics engine with rigid bodies, static colliders, gravity, and impulses
- Tilemap rendering and brush placement tools

## Milestone 4: Multi-Provider AI Agent System 🚧
- Abstract `AiProvider` interface and configuration system
- Integration drivers: OpenAI, Gemini, Claude, DeepSeek, Custom OpenAI, Local LLMs
- `AiAgentEngine` intent parser for scene modification, script creation, error fixing, and project search

## Milestone 5: Code Editor, Debug Console & APK Export 🚧
- Built-in GDScript / Lua / Kotlin code editor with syntax highlighting
- Live debug console with error reporting and AI explanation hooks
- Direct on-device APK export module

## Milestone 6: Quality Assurance & Optimization 🚧
- Unit testing for engine core, AI provider layer, and serialization
- Performance optimizations for particle systems and physics collision resolution
