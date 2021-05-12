package com.github.sukhinin.krabatron

import kotlinx.coroutines.runBlocking

interface RequestExecutor<T> {
    suspend fun get(request: Request): T
}

suspend fun <T> RequestExecutor<T>.get(url: String): T {
    return get(Request(url))
}

fun <T> RequestExecutor<T>.getBlocking(request: Request): T {
    val executor = this
    return runBlocking { executor.get(request) }
}

fun <T> RequestExecutor<T>.getBlocking(url: String): T {
    val executor = this
    return runBlocking { executor.get(url) }
}
