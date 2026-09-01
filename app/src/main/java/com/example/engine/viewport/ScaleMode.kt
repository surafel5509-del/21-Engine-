package com.example.engine.viewport

/**
 * Strategy for scaling the logical game resolution onto various physical device screens and window sizes.
 */
enum class ScaleMode {
    /**
     * Preserves aspect ratio by adding letterboxing (horizontal black/colored bars) or pillarboxing (vertical bars).
     * Recommended default for most 2D games with fixed design resolution (e.g., 1280x720).
     */
    FIT_LETTERBOX,

    /**
     * Preserves aspect ratio by scaling up to completely fill the screen, cropping any overflow.
     */
    FILL_CROP,

    /**
     * Stretches non-uniformly to fill the entire physical display without any black bars.
     */
    STRETCH,

    /**
     * Maintains aspect ratio by extending the visible logical game world in the wider/taller axis.
     */
    EXTEND_VIEWPORT,

    /**
     * Fits using integer scaling factors (1x, 2x, 3x, etc.) for pixel-perfect retro aesthetics.
     */
    INTEGER_SCALE
}
