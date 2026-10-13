# Development Roadmap

## Milestone 1: Core Engine Architecture & Architecture Docs
- Setup modular project architecture (`engine`, `studio`, `studio.ai`).
- Establish 2D Math primitives (Vector2, Rect2D, Transform2D, Circle2D, Ray2D).
- Implement hardware-accelerated Canvas2D renderer & game loop.
- Implement physics core (AABB, RigidBody2D, Impulse solver).
- Document system architecture, README, ROADMAP, and TODO.

## Milestone 2: Scene & Component System
- Hierarchical scene graph (`GameObject`, `Scene`, `Component`).
- Engine components: `SpriteRendererComponent`, `RigidBody2DComponent`, `Collider2DComponent`, `ScriptComponent`, `ParticleEmitterComponent`, `TextRendererComponent`.
- Viewport management (ScaleMode, Safe Area Insets).

## Milestone 3: Visual Game Studio UI & Docks
- Jetpack Compose touch-first visual editor UI.
- Scene Tree Dock (node parenting, add/remove/reorder nodes).
- Inspector Dock (live parameter tweaking, transform gizmos, component controls).
- Asset Library Dock & Custom File Storage Manager.

## Milestone 4: Scripting & Visual Logic Systems
- Dynamic GDScript/Lua-like scripting execution engine.
- Visual Scripting node graph editor with logic connections.
- Code Editor with syntax highlighting and live error reporting.

## Milestone 5: Audio, Animation & Particle Systems
- Multi-channel audio manager (`SoundPool` & `MediaPlayer`).
- Animated sprite sheet editor and timeline keyframing.
- Particle system emitter editor (color ramps, speed, spread, lifetime).

## Milestone 6: On-Device APK Export & AI Agent Framework
- Android build wrapper for on-device APK generation and game packaging.
- Multi-provider AI Assistant framework (OpenAI, Gemini, Claude, DeepSeek, Local Heuristic).
- Project templates (2D Platformer, Physics Sandbox, Performance Benchmark).
