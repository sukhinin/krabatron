package com.github.sukhinin.krabatron

import com.github.sukhinin.krabatron.proxy.Proxy
import com.github.sukhinin.krabatron.proxy.ProxyManager
import com.nhaarman.mockitokotlin2.*
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

internal class ProxyManagerDecoratorTest : ShouldSpec({

    should("pass response received from delegate request executor") {
        val response = "ok"
        val mock = mock<RequestExecutor<String>> {
            onBlocking { get(any()) } doReturn response
        }

        val proxy = Proxy(Proxy.Type.HTTP, "127.0.0.1:8080")
        val manager = ProxyManager(listOf(proxy))
        val executor = mock.withProxyManager(manager)
        val request = Request("http://127.0.0.1:80")

        executor.get(request) shouldBe response
    }

    should("rethrow exception thrown from delegate request executor") {
        val error = RuntimeException("Simulated error")
        val mock = mock<RequestExecutor<String>> {
            onBlocking { get(any()) } doThrow error
        }

        val proxy = Proxy(Proxy.Type.HTTP, "127.0.0.1:8080")
        val manager = ProxyManager(listOf(proxy))
        val executor = mock.withProxyManager(manager)
        val request = Request("http://127.0.0.1:80")

        val thrown = shouldThrowAny { executor.get(request) }
        thrown shouldBeSameInstanceAs error
    }

    should("set proxy when request does not specify it") {
        val mock = mock<RequestExecutor<String>> {
            onBlocking { get(any()) } doReturn "ok"
        }

        val proxy = Proxy(Proxy.Type.HTTP, "127.0.0.1:8080")
        val manager = ProxyManager(listOf(proxy))
        val executor = mock.withProxyManager(manager)
        val request = Request("http://127.0.0.1:80", proxy = null)
        executor.get(request)

        verify(mock).get(request.copy(proxy = proxy))
    }

    should("not change proxy when request already specifies one") {
        val mock = mock<RequestExecutor<String>> {
            onBlocking { get(any()) } doReturn "ok"
        }

        val proxy = Proxy(Proxy.Type.HTTP, "127.0.0.1:8080")
        val manager = ProxyManager(listOf(proxy))
        val executor = mock.withProxyManager(manager)
        val request = Request("http://127.0.0.1:80", proxy = Proxy(Proxy.Type.SOCKS5, "127.0.0.1:3128"))
        executor.get(request)

        verify(mock).get(request)
    }
})