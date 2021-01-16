package com.github.sukhinin.krabatron.proxy

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.shouldNot
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.time.Duration

internal class ProxyManagerTest : ShouldSpec({

    should("throw when provided with no proxies") {
        shouldThrow<IllegalArgumentException> { ProxyManager(emptyList()) }
    }

    should("immediately acquire proxy when available") {
        val proxies = listOf(Proxy(Proxy.Type.HTTP, "127.0.0.1:8080"))
        val manager = ProxyManager(proxies)
        manager.acquire() shouldNot beNull()
    }

    should("suspend when proxy is not available") {
        val proxies = listOf(Proxy(Proxy.Type.HTTP, "127.0.0.1:8080"))
        val manager = ProxyManager(proxies)
        manager.acquire()

        shouldThrow<TimeoutCancellationException> {
            withTimeout(100) {
                manager.acquire()
            }
        }
    }

    should("resume suspended coroutine when proxy becomes available") {
        val proxies = listOf(Proxy(Proxy.Type.HTTP, "127.0.0.1:8080"))
        val manager = ProxyManager(proxies, delayBeforeReuse = Duration.ZERO)

        val proxy = manager.acquire()
        launch {
            delay(50)
            manager.release(proxy)
        }

        withTimeout(100) {
            manager.acquire()
        }
    }

    should("wait for reuse delay in separate coroutine") {
        val proxies = listOf(Proxy(Proxy.Type.HTTP, "127.0.0.1:8080"))
        val manager = ProxyManager(proxies, delayBeforeReuse = Duration.ofSeconds(1))

        withTimeout(100) {
            val proxy = manager.acquire()
            manager.release(proxy)
        }
    }
})