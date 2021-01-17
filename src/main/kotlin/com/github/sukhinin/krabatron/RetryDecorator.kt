package com.github.sukhinin.krabatron

import java.util.*

class RetryDecorator<T>(
    private val delegate: RequestExecutor<T>,
    private val maxRetries: Int
) : RequestExecutor<T> {

    override suspend fun get(request: Request): T {
        val errors = LinkedList<Exception>()

        while (errors.size <= maxRetries) {
            try {
                return delegate.get(request)
            } catch (e: Exception) {
                errors.add(e)
            }
        }

        val exception = TooManyErrorsException("Retry limit exceeded")
        errors.forEach(exception::addSuppressed)
        throw exception
    }
}

fun <T> RequestExecutor<T>.retryOnError(maxRetries: Int): RequestExecutor<T> {
    return RetryDecorator(this, maxRetries)
}
