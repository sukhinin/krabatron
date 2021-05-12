package com.github.sukhinin.krabatron

import com.nhaarman.mockitokotlin2.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

internal class OnSuccessDecoratorTest : ShouldSpec({

    should("invoke callback on successful delegate execution") {
        val response = "ok"
        val mock = mock<RequestExecutor<String>> {
            onBlocking { get(any()) } doReturn response
        }

        val callback = mock<(String) -> Unit> {
            onBlocking { invoke(any()) } doReturn Unit
        }

        val executor = mock.onSuccess(callback)
        val request = Request("http://127.0.0.1:80")
        executor.get(request) shouldBe response

        verify(callback).invoke(any())
        verifyNoMoreInteractions(callback)
    }

    should("not invoke callback on failed delegate execution") {
        val message = "Simulated error"
        val mock = mock<RequestExecutor<String>> {
            onBlocking { get(any()) } doThrow RuntimeException(message)
        }

        val callback = mock<(String) -> Unit> {
            onBlocking { invoke(any()) } doReturn Unit
        }

        val executor = mock.onSuccess(callback)
        val request = Request("http://127.0.0.1:80")
        val error = shouldThrow<RuntimeException> { executor.get(request) }
        error.message shouldBe message

        verifyZeroInteractions(callback)
    }
})
