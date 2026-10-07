# Architecture Document

## Overview

The Android 2D Game Engine and Studio is designed with strict separation of concerns between the **Engine Runtime Core** (`com.example.engine`) and the **Android Studio Visual Editor** (`com.example.studio`).

---

## 1. Engine Core Layer (`com.example.engine`)

- **Core & Loop**: `GameEngine` orchestrates fixed physics update ticks and variable render frame callbacks on `GameSurfaceView`.
- **Renderer**: `Canvas2DRenderer` renders `Sprite`, `Shape`, `Text`, and particle systems onto Android Canvas with transformation matrix stacks.
- **Physics**: `PhysicsWorld2D` manages 2D rigid bodies, AABB/Circle collision manifolds, restitution, friction, impulse resolution, and gravity integration.
- **Scene & Node Graph**: `Scene` manages active `GameObject` hierarchy and `Component` lifecycles (`onStart`, `onUpdate`, `onRender`).
- **Audio**: `AudioManager` handles SoundPool audio playback and real-time procedural sound synthesizer generation.
- **UI Framework**: Immediate/Retained UI system for game HUD elements (`UIButton`, `UISlider`, `UIProgressBar`, `UIJoystick`).

---

## 2. Studio Visual Editor (`com.example.studio`)

- **Touch Viewport**: Interactive 2D editing scene (`StudioRuntimeScene`) supporting pan, zoom, grid snapping, tool modes (`SELECT_MOVE`, `BRUSH_PLACE`).
- **Dockable Panels**: Modular Compose dock tabs for:
  - **Hierarchy**: Node graph creation, duplication, reordering.
  - **Inspector**: Real-time property editing (transforms, physics, shapes, scripts).
  - **Code Editor**: Live GDScript/Lua script editing and console evaluation.
  - **Tilemap**: Grid tile painter with color/texture palettes.
  - **Sprite Anim**: Frame splitter and FPS keyframe manager.
  - **Assets & FX**: Texture loader and sound FX previewer.
  - **Export**: Package configuration and APK compilation trigger.

---

## 3. AI Agent Architecture (`com.example.studio.ai`)

- **Provider Abstraction**: `AiAgentProvider` interface decouples AI logic from specific cloud or local APIs.
- **Supported Providers**:
  - `OpenAI` (`gpt-4o`)
  - `Google Gemini` (`gemini-1.5-pro`)
  - `Anthropic Claude` (`claude-3-5-sonnet`)
  - `DeepSeek` (`deepseek-chat`)
  - `Local LLM` (`Ollama` / local HTTP endpoint)
- **Engine Agent Command Dispatch**: `AiGameDevAgent` parses assistant structured responses (`CREATE_NODE`, `UPDATE_SCRIPT`, `MODIFY_PHYSICS`) and applies mutations directly to `GameProject` node graph.
