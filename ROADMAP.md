# Product & Development Roadmap

## Milestone 1: Engine Architecture & Core Subsystems (Completed)
- [x] Canvas2D double-buffered rendering pipeline
- [x] Scene manager & GameObject component system
- [x] 2D camera viewport scaling & letterboxing
- [x] 2D Physics world with AABB collisions and impulse resolution
- [x] Procedural sound effect synthesizer
- [x] Touch gesture detector & virtual joystick input system

## Milestone 2: Android Studio Visual Editor (Completed)
- [x] Touch-optimized Jetpack Compose visual studio UI
- [x] Real-time 2D viewport editor with select, move, and brush placement tools
- [x] Scene tree hierarchy dock with node creation, duplication, and reordering
- [x] Live inspector dock for transform, physics, and visual parameters
- [x] Instant live play mode toggle with restart and exit-to-editor support
- [x] Custom asset library manager and sprite sheet slice preview
- [x] Visual script logic editor (On Touch, On Collision, Auto Move, Spawner)
- [x] Pre-packaged game templates (2D Platformer, Physics Sandbox, Stress Benchmark)

## Milestone 3: AI Assistant Layer (In Progress)
- [x] Provider-agnostic AI agent framework (`AiProvider`, `AiAction`, `AiContext`)
- [ ] Implement local offline rule-based AI provider (`RuleBasedLocalAiProvider`)
- [ ] Implement generic REST API provider (`OpenAiCompatibleProvider` for OpenAI, Anthropic, DeepSeek, Local LLM)
- [ ] Implement Google Gemini provider (`FirebaseGeminiProvider`)
- [ ] Engine tool execution engine (`EngineAiAssistant`)
- [ ] Studio Compose UI dock for AI Assistant (`AiAssistantDock`)

## Milestone 4: Advanced Engine Tools & Export (Upcoming)
- [ ] Tilemap visual editor dock with auto-tiling and tile collision bounds
- [ ] Timeline-based sprite keyframe animation editor
- [ ] Particle system designer with gravity, radial acceleration, and color ramps
- [ ] Direct APK build/export trigger from Android application
- [ ] Project import/export zip archiving
