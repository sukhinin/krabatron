package com.github.sukhinin.krabatron

class TransformDecorator<T, R>(
    private val delegate: RequestExecutor<T>,
    private val mapper: (T) -> R
) : RequestExecutor<R> {

    override suspend fun get(request: Request): R {
        return delegate.get(request).let(mapper)
    }
}

fun <T, R> RequestExecutor<T>.transform(mapper: (T) -> R): RequestExecutor<R> {
    return TransformDecorator(this, mapper)
}
