package com.github.sukhinin.krabatron

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

internal class TransformDecoratorTest : ShouldSpec({

    should("transform response received from delegate request executor") {
        val response = "ok"
        val mock = mock<RequestExecutor<String>> {
            onBlocking { get(any()) } doReturn response
        }

        val executor = mock.transform { it + it }
        val request = Request("http://127.0.0.1:80")
        executor.get(request) shouldBe response + response
    }

    should("pass through exceptions from underlying delegate") {
        val message = "Simulated error"
        val mock = mock<RequestExecutor<String>> {
            onBlocking { get(any()) } doThrow RuntimeException(message)
        }

        val executor = mock.transform { it + it }
        val request = Request("http://127.0.0.1:80")

        val error = shouldThrow<RuntimeException> { executor.get(request) }
        error.message shouldBe message
    }

    should("throw its own exceptions") {
        val response = "ok"
        val mock = mock<RequestExecutor<String>> {
            onBlocking { get(any()) } doReturn response
        }

        val message = "Simulated error"
        val executor = mock.transform { throw RuntimeException(message) }
        val request = Request("http://127.0.0.1:80")

        val error = shouldThrow<RuntimeException> { executor.get(request) }
        error.message shouldBe message
    }
})
