package com.github.sukhinin.krabatron

class OnFailureDecorator<T>(
    private val delegate: RequestExecutor<T>,
    private val callback: (Request, Throwable) -> Unit
) : RequestExecutor<T> {

    override suspend fun get(request: Request): T {
        return runCatching { delegate.get(request) }
            .onFailure { error -> callback(request, error) }
            .getOrThrow()
    }
}

fun <T> RequestExecutor<T>.onFailure(callback: (Request, Throwable) -> Unit): RequestExecutor<T> {
    return OnFailureDecorator(this, callback)
}
