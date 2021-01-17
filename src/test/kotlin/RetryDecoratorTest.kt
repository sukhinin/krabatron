import com.nhaarman.mockitokotlin2.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.lang.RuntimeException

internal class RetryDecoratorTest : ShouldSpec({

    should("pass response received from delegate request executor") {
        val response = "ok"
        val mock = mock<RequestExecutor<String>> {
            onBlocking { get(any()) } doReturn response
        }

        val executor = mock.retryOnError(3)
        val request = Request("http://127.0.0.1:80")
        executor.get(request) shouldBe response
    }

    should("retry request in case of an error") {
        val response = "ok"
        val mock = mock<RequestExecutor<String>> {
            onBlocking { get(any()) }
                .doThrow(RuntimeException("Simulated error"))
                .doReturn(response)
        }

        val executor = mock.retryOnError(3)
        val request = Request("http://127.0.0.1:80")

        executor.get(request) shouldBe response
        verify(mock, times(2)).get(request)
    }

    should("throw on exceeding max retries limit") {
        val mock = mock<RequestExecutor<String>> {
            onBlocking { get(any()) } doThrow RuntimeException("Simulated error")
        }

        val maxRetries = 3
        val executor = mock.retryOnError(maxRetries)
        val request = Request("http://127.0.0.1:80")

        val error = shouldThrow<TooManyErrorsException> { executor.get(request) }
        error.suppressed.size shouldBe maxRetries + 1

        verify(mock, times(maxRetries + 1)).get(request)
    }
})