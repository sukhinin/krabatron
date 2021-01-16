package com.github.sukhinin.krabatron.proxy

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe

internal class ProxyHealthTest : ShouldSpec({

    should("have neutral initial score by default") {
        ProxyHealth().score shouldBe 0.5
    }

    should("increase score on success") {
        val health = ProxyHealth()
        val before = health.score
        health.adjust(ProxyHealth.Result.SUCCESS)
        val after = health.score
        before shouldBeLessThan after
    }

    should("decrease score on failure") {
        val health = ProxyHealth()
        val before = health.score
        health.adjust(ProxyHealth.Result.FAILURE)
        val after = health.score
        before shouldBeGreaterThan after
    }
})