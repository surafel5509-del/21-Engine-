# GodotStudio Development TODO List

## Core Engine & Runtime
- [x] Basic game engine loop and surface view
- [x] Vector2, Transform2D, and Rect2D math utilities
- [x] Hardware-accelerated Canvas2D renderer
- [x] 2D Physics world with AABB collisions and impulse resolution
- [x] AudioManager with built-in synth sounds
- [x] UI Designer elements (UIButton, UILabel, UIPanel, UIProgressBar, UIJoystick)
- [x] CPU Particle Emitter component

## Editor UI & Tools
- [x] Godot-inspired dark studio UI theme
- [x] Scene Tree Dock (add, rename, duplicate, reorder, delete nodes)
- [x] Inspector Dock (transform, physics, particles, sprite assets)
- [x] Viewport interactive gizmos, camera zoom/pan, grid snap, brush tool
- [x] Asset Library Dock with built-in texture and SFX browser
- [x] Templates Dock (Platformer, Physics Sandbox, Stress Benchmark)
- [ ] Code Editor Dock with GDScript syntax highlighting and line numbers
- [ ] Debug Console Dock with engine metrics and error logs

## AI Agent System
- [ ] Implement `AiProvider` interface
- [ ] Implement drivers for OpenAI, Gemini, Claude, DeepSeek, Custom API, Local LLM
- [ ] Implement `AiAgentEngine` for parsing actions (create node, modify scene, generate script, search project, fix code)
- [ ] Create AI Assistant Dock UI with provider/model/API key configuration and interactive chat

## Export & Build
- [ ] Implement `ApkExportManager` and `ApkExportModal.kt`
- [ ] On-device game packaging setup

## Testing & Documentation
- [x] Architecture documentation (`README.md`, `ARCHITECTURE.md`, `ROADMAP.md`, `TODO.md`)
- [ ] Unit tests for `AiProviderManager` and `AiAgentEngine`
- [ ] Build & screenshot verification
