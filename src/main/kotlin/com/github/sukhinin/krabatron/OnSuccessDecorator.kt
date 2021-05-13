package com.github.sukhinin.krabatron

class OnSuccessDecorator<T>(
    private val delegate: RequestExecutor<T>,
    private val callback: (Request, T) -> Unit
) : RequestExecutor<T> {

    override suspend fun get(request: Request): T {
        return runCatching { delegate.get(request) }
            .onSuccess { response -> callback(request, response) }
            .getOrThrow()
    }
}

fun <T> RequestExecutor<T>.onSuccess(callback: (Request, T) -> Unit): RequestExecutor<T> {
    return OnSuccessDecorator(this, callback)
}
