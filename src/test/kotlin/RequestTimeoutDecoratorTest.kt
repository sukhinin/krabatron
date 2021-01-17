import com.nhaarman.mockitokotlin2.*
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import java.lang.RuntimeException
import java.time.Duration

internal class RequestTimeoutDecoratorTest : ShouldSpec({

    should("pass response received from delegate request executor") {
        val response = "ok"
        val mock = mock<RequestExecutor<String>> {
            onBlocking { get(any()) } doReturn response
        }

        val timeout = Duration.ofMillis(1234)
        val executor = mock.withRequestTimeout(timeout)
        val request = Request("http://127.0.0.1:80")

        executor.get(request) shouldBe response
    }

    should("rethrow exception thrown from delegate request executor") {
        val error = RuntimeException("Simulated error")
        val mock = mock<RequestExecutor<String>> {
            onBlocking { get(any()) } doThrow error
        }

        val timeout = Duration.ofMillis(1234)
        val executor = mock.withRequestTimeout(timeout)
        val request = Request("http://127.0.0.1:80")

        val thrown = shouldThrowAny { executor.get(request) }
        thrown shouldBeSameInstanceAs error
    }

    should("set timeout when request does not specify it") {
        val mock = mock<RequestExecutor<String>> {
            onBlocking { get(any()) } doReturn "ok"
        }

        val timeout = Duration.ofMillis(1234)
        val executor = mock.withRequestTimeout(timeout)
        val request = Request("http://127.0.0.1:80", timeout = null)
        executor.get(request)

        verify(mock).get(request.copy(timeout = timeout))
    }

    should("not change timeout when request already specifies one") {
        val mock = mock<RequestExecutor<String>> {
            onBlocking { get(any()) } doReturn "ok"
        }

        val timeout = Duration.ofMillis(1234)
        val executor = mock.withRequestTimeout(timeout)
        val request = Request("http://127.0.0.1:80", timeout = Duration.ofMillis(5678))
        executor.get(request)

        verify(mock).get(request)
    }
})