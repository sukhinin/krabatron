package com.github.sukhinin.krabatron

interface RequestExecutor<T> {
    suspend fun get(request: Request): T
}
