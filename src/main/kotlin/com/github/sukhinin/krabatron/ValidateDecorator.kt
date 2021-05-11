package com.github.sukhinin.krabatron

class ValidateDecorator<T>(
    private val delegate: RequestExecutor<T>,
    private val validator: (T) -> Unit
) : RequestExecutor<T> {

    override suspend fun get(request: Request): T {
        return delegate.get(request).also(validator)
    }
}

fun <T> RequestExecutor<T>.validate(validator: (T) -> Unit): RequestExecutor<T> {
    return ValidateDecorator(this, validator)
}
