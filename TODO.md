# TODO

## Engine Core & Runtime
- [x] Maintain Canvas2D renderer stability across aspect ratios
- [x] Verify physics manifold resolution for tile map edge collisions
- [ ] Add support for custom shader materials in GLES pipeline

## Studio Visual Editor
- [x] Implement GDScript/Lua code editor dock with console output
- [x] Build Tilemap painter dock with tile palette grid
- [x] Build Sprite Animation keyframe editor dock
- [x] Build Android APK export settings dock

## AI Assistant Layer
- [x] Implement multi-provider `AiAgentProvider` abstraction
- [x] Support OpenAI, Gemini, Claude, DeepSeek, and Local LLM (Ollama) endpoints
- [x] Implement agent JSON command parser for automated scene node creation and script modification
- [x] Add AI Assistant chat dock UI to studio screen

## Tests & Packaging
- [x] Maintain unit test coverage for vector math, physics collision, project node CRUD, and AI commands
- [x] Ensure `gradle assembleDebug` builds cleanly
