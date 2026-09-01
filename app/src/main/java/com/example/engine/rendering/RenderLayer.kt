package com.example.engine.rendering

/**
 * Standard visual layers for 2D rendering and z-ordering.
 */
enum class RenderLayer(val order: Int) {
    BACKGROUND(0),
    WORLD_BACK(100),
    WORLD_MAIN(200),
    WORLD_FRONT(300),
    FOREGROUND(400),
    UI(500),
    DEBUG_OVERLAY(600)
}
