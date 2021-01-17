import com.github.sukhinin.krabatron.proxy.ProxyHealth
import com.github.sukhinin.krabatron.proxy.ProxyManager

class ProxyManagerDecorator<T>(
    private val delegate: RequestExecutor<T>,
    private val proxyManager: ProxyManager
) : RequestExecutor<T> {

    override suspend fun get(request: Request): T {
        if (request.proxy != null) {
            return delegate.get(request)
        }

        val proxy = proxyManager.acquire()
        try {
            val response = delegate.get(request.copy(proxy = proxy))
            proxyManager.release(proxy, ProxyHealth.Result.SUCCESS)
            return response
        } catch (e: Exception) {
            proxyManager.release(proxy, ProxyHealth.Result.FAILURE)
            throw e
        }
    }
}

fun <T> RequestExecutor<T>.withProxyManager(proxyManager: ProxyManager): RequestExecutor<T> {
    return ProxyManagerDecorator(this, proxyManager)
}
