import com.github.sukhinin.krabatron.proxy.Proxy
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.asynchttpclient.*
import org.asynchttpclient.proxy.ProxyServer
import org.asynchttpclient.proxy.ProxyType
import java.io.IOException
import java.util.concurrent.ExecutionException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class Krabatron(client: AsyncHttpClient? = null) : RequestExecutor<ByteArray>, AutoCloseable {

    companion object {
        private fun createDefaultClient(): AsyncHttpClient {
            val config = Dsl.config()
                .setFollowRedirect(true)
                .setMaxRedirects(3)
            return Dsl.asyncHttpClient(config)
        }
    }

    private val client = client ?: createDefaultClient()
    private val managesClient = client == null

    override suspend fun get(request: Request): ByteArray {
        val response = suspendCancellableCoroutine<Response> { continuation ->
            val future = getBoundRequest(request).execute()
            val handler = { handleResponse(future, continuation) }
            future.addListener(handler, null)
        }

        if (response.statusCode < 200 || response.statusCode > 299) {
            throw IOException("Bad HTTP status: ${response.statusCode} ${response.statusText}")
        }

        return response.responseBodyAsBytes
    }

    private fun handleResponse(future: ListenableFuture<Response>, continuation: CancellableContinuation<Response>) {
        try {
            continuation.resume(future.get())
        } catch (e: Exception) {
            val error = (e as? ExecutionException)?.cause ?: e
            future.abort(error)
            continuation.resumeWithException(error)
        }
    }

    private fun getBoundRequest(request: Request): BoundRequestBuilder {
        val boundRequest = client.prepareGet(request.url)
        if (request.proxy != null) {
            val proxyServer = getProxyServer(request.proxy)
            boundRequest.setProxyServer(proxyServer)
        }
        if (request.timeout != null) {
            val requestTimeoutMillis = request.timeout.toMillis()
            boundRequest.setRequestTimeout(requestTimeoutMillis.toInt())
        }
        return boundRequest
    }

    private fun getProxyServer(proxy: Proxy): ProxyServer {
        val proxyType = when (proxy.type) {
            Proxy.Type.HTTP -> ProxyType.HTTP
            Proxy.Type.SOCKS4 -> ProxyType.SOCKS_V4
            Proxy.Type.SOCKS5 -> ProxyType.SOCKS_V5
        }
        return ProxyServer.Builder(proxy.host, proxy.port)
            .setProxyType(proxyType)
            .build()
    }

    override fun close() {
        if (managesClient) {
            client.close()
        }
    }
}
