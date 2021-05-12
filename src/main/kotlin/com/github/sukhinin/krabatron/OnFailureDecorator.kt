package com.github.sukhinin.krabatron

class OnFailureDecorator<T>(
    private val delegate: RequestExecutor<T>,
    private val callback: (Throwable) -> Unit
) : RequestExecutor<T> {

    override suspend fun get(request: Request): T {
        return runCatching { delegate.get(request) }
            .onFailure(callback)
            .getOrThrow()
    }
}

fun <T> RequestExecutor<T>.onFailure(callback: (Throwable) -> Unit): RequestExecutor<T> {
    return OnFailureDecorator(this, callback)
}
