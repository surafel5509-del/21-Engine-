package com.example.engine.utils

/**
 * High-performance generic object pool to recycle instances and avoid garbage collection overhead in the render/game loop.
 */
class Pool<T>(
    private val initialCapacity: Int = 16,
    private val maxCapacity: Int = 256,
    private val factory: () -> T,
    private val reset: ((T) -> Unit)? = null
) {
    private val freeObjects = ArrayList<T>(initialCapacity)

    init {
        for (i in 0 until initialCapacity) {
            freeObjects.add(factory())
        }
    }

    fun obtain(): T {
        return if (freeObjects.isEmpty()) {
            factory()
        } else {
            freeObjects.removeAt(freeObjects.size - 1)
        }
    }

    fun free(obj: T) {
        if (freeObjects.size < maxCapacity) {
            reset?.invoke(obj)
            freeObjects.add(obj)
        }
    }

    fun clear() {
        freeObjects.clear()
    }

    val freeCount: Int get() = freeObjects.size
}
