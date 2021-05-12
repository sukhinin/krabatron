package com.github.sukhinin.krabatron

class OnSuccessDecorator<T>(
    private val delegate: RequestExecutor<T>,
    private val callback: (T) -> Unit
) : RequestExecutor<T> {

    override suspend fun get(request: Request): T {
        return runCatching { delegate.get(request) }
            .onSuccess(callback)
            .getOrThrow()
    }
}

fun <T> RequestExecutor<T>.onSuccess(callback: (T) -> Unit): RequestExecutor<T> {
    return OnSuccessDecorator(this, callback)
}
