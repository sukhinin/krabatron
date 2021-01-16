package com.github.sukhinin.krabatron.proxy

data class Proxy(val type: Type, val host: String, val port: Int) {
    companion object {
        operator fun invoke(type: Type, hostAndPort: String) = try {
            val (host, port) = hostAndPort.split(':', limit = 2)
            Proxy(type, host, port.toInt())
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid address format: $hostAndPort", e)
        }
    }

    enum class Type { HTTP, SOCKS4, SOCKS5 }
}
