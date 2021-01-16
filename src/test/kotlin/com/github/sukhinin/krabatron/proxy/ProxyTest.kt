package com.github.sukhinin.krabatron.proxy

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

internal class ProxyTest : ShouldSpec({

    should("parse host and port") {
        val proxy = Proxy(Proxy.Type.HTTP, "127.0.0.1:8080")
        proxy.host shouldBe "127.0.0.1"
        proxy.port shouldBe 8080
    }

    should("throw on invalid format") {
        shouldThrow<IllegalArgumentException> { Proxy(Proxy.Type.HTTP, "127.0.0.1") }
        shouldThrow<IllegalArgumentException> { Proxy(Proxy.Type.HTTP, "127.0.0.1:8080:8081") }
        shouldThrow<IllegalArgumentException> { Proxy(Proxy.Type.HTTP, "127.0.0.1:abcd") }
    }
})