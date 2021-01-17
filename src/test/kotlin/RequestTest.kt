import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

internal class RequestTest : ShouldSpec({

    should("auto generate unique id") {
        val request1 = Request("http://127.0.0.1:80")
        val request2 = Request("http://127.0.0.1:80")
        request1.id shouldNotBe request2.id
    }

    should("allow overriding auto generated id") {
        val id = "my id"
        val request = Request("http://127.0.0.1:80", id = "my id")
        request.id shouldBe id
    }
})