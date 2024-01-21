package com.gugas.takeaway

import org.mockito.Mockito

// Is needed as Mockito has problems with mocking Kotlin classes
object MockitoHelper {
    fun <T> any(): T {
        Mockito.any<T>()
        return uninitialized()
    }
    @Suppress("UNCHECKED_CAST")
    private fun <T> uninitialized(): T = null as T
}