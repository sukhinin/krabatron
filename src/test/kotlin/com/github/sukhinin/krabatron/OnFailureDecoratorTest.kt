package com.github.sukhinin.krabatron

import com.nhaarman.mockitokotlin2.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

internal class OnFailureDecoratorTest : ShouldSpec({

    should("not invoke callback on successful delegate execution") {
        val response = "ok"
        val mock = mock<RequestExecutor<String>> {
            onBlocking { get(any()) } doReturn response
        }

        val callback = mock<(Throwable) -> Unit> {
            onBlocking { invoke(any()) } doReturn Unit
        }

        val executor = mock.onFailure(callback)
        val request = Request("http://127.0.0.1:80")
        executor.get(request) shouldBe response

        verifyZeroInteractions(callback)
    }

    should("invoke callback on failed delegate execution") {
        val message = "Simulated error"
        val mock = mock<RequestExecutor<String>> {
            onBlocking { get(any()) } doThrow RuntimeException(message)
        }

        val callback = mock<(Throwable) -> Unit> {
            onBlocking { invoke(any()) } doReturn Unit
        }

        val executor = mock.onFailure(callback)
        val request = Request("http://127.0.0.1:80")
        val error = shouldThrow<RuntimeException> { executor.get(request) }
        error.message shouldBe message

        verify(callback).invoke(argWhere { e -> e is RuntimeException && e.message == message })
        verifyNoMoreInteractions(callback)
    }
})
