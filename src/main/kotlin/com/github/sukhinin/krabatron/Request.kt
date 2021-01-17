package com.github.sukhinin.krabatron

import com.github.sukhinin.krabatron.proxy.Proxy
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

data class Request(
    val url: String,
    val proxy: Proxy? = null,
    val timeout: Duration? = null,
    val id: String = nextId()
) {
    companion object {
        private val counter = AtomicInteger(0)
        fun nextId() = "req ${counter.incrementAndGet()}"
    }
}
