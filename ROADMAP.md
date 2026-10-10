# Development Roadmap

## Phase 1: Architecture & Engine Core Basics (Completed)
- [x] Initial Android project structure with Kotlin and Jetpack Compose.
- [x] Engine loop, Canvas2D renderer, viewport scaling, camera 2D.
- [x] Scene hierarchy, transform hierarchy, and node model.
- [x] 2D Physics engine with box/circle collision detection and resolution.
- [x] Basic audio synthesizer and audio manager.

## Phase 2: Engine System Expansion (Completed)
- [x] Lightweight GDScript/Lua-like text script interpreter and execution engine.
- [x] Tilemap component and multi-layer tile renderer.
- [x] Reusable Node Prefab manager.

## Phase 3: Studio Editor Docks (Completed)
- [x] Scene Tree Hierarchy dock.
- [x] Inspector dock with live node properties editing.
- [x] Built-in GDScript code editor dock with syntax highlighting and linting.
- [x] Visual Tilemap Editor dock with tile palette and layer control.
- [x] Asset Library & FX dock.
- [x] Debug Console & error log output dock.
- [x] Project Manager & standalone APK Exporter dock.

## Phase 4: Pluggable AI Copilot System (Completed)
- [x] `AiProvider` interface abstraction.
- [x] Integrations for OpenAI, Google Gemini, Anthropic Claude, DeepSeek, Local OpenAI API, and Offline Local Heuristic AI.
- [x] `AiAgent` tool call system (Search, Scene creation, Script writing, Error fixing, Build testing).
- [x] AI Assistant Dock UI with prompt presets and chat interface.

## Phase 5: Testing & Verification (Completed)
- [x] Unit tests for Engine Core, Physics, ScriptEngine, and AI Agent tool calls.
- [x] Build verification via `gradle testDebugUnitTest` and `gradle assembleDebug`.
