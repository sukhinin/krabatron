import java.time.Duration

class RequestTimeoutDecorator<T>(
    private val delegate: RequestExecutor<T>,
    private val timeout: Duration
) : RequestExecutor<T> {

    override suspend fun get(request: Request): T {
        return when(request.timeout) {
            null -> delegate.get(request.copy(timeout = this.timeout))
            else -> delegate.get(request)
        }
    }
}

fun <T> RequestExecutor<T>.withRequestTimeout(timeout: Duration): RequestExecutor<T> {
    return RequestTimeoutDecorator(this, timeout)
}
