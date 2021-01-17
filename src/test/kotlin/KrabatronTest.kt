import io.javalin.Javalin
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.asynchttpclient.handler.MaxRedirectException
import java.io.IOException
import java.net.ConnectException
import java.time.Duration
import java.util.concurrent.TimeoutException

internal class KrabatronTest : ShouldSpec({

    val javalin = Javalin.create().start()
    afterSpec { javalin.stop() }

    javalin.get("/200") { ctx -> ctx.result("200 ok") }
    javalin.get("/200-slow") { ctx -> Thread.sleep(500); ctx.result("200 ok") }
    javalin.get("/301") { ctx -> ctx.redirect("/200") }
    javalin.get("/301-loop") { ctx -> ctx.redirect("/301-loop") }
    javalin.get("/500") { ctx -> ctx.status(500) }

    should("execute get requests") {
        val krabatron = Krabatron()
        val request = Request("http://localhost:${javalin.port()}/200")
        val response = krabatron.get(request)
        String(response) shouldBe "200 ok"
    }

    should("follow redirects") {
        val krabatron = Krabatron()
        val request = Request("http://localhost:${javalin.port()}/301")
        val response = krabatron.get(request)
        String(response) shouldBe "200 ok"
    }

    should("throw on redirect loop") {
        val krabatron = Krabatron()
        val request = Request("http://localhost:${javalin.port()}/301-loop")
        shouldThrow<MaxRedirectException> { krabatron.get(request) }
    }

    should("throw on bad status code") {
        val krabatron = Krabatron()
        val request = Request("http://127.0.0.1:${javalin.port()}/500")
        shouldThrow<IOException> { krabatron.get(request) }
    }

    should("throw when unable to connect") {
        val krabatron = Krabatron()
        val request = Request("http://127.0.0.1:1/")
        shouldThrow<ConnectException> { krabatron.get(request) }
    }

    should("throw on request timeout") {
        val krabatron = Krabatron()
        val request = Request("http://localhost:${javalin.port()}/200-slow", timeout = Duration.ofMillis(1))
        shouldThrow<TimeoutException> { krabatron.get(request) }
    }
})