package com.github.sukhinin.krabatron.proxy

import kotlinx.coroutines.*
import java.time.Duration
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.LinkedHashSet
import kotlin.coroutines.resume

class ProxyManager(
    proxies: Collection<Proxy>,
    private val delayBeforeReuse: Duration = Duration.ofMillis(3000),
    private val scoreBias: Double = 0.01
) {

    companion object {
        // One executor should be enough for all proxy manager instances. Must be single
        // threaded to ensure safe concurrent calls, otherwise synchronization required.
        private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    }

    init {
        require(proxies.isNotEmpty()) { "Must provide at least one proxy to manage" }
    }

    private val managedProxies: Map<Proxy, ProxyHolder> = proxies.associateWith { proxy -> ProxyHolder(proxy) }
    private val availableProxies: LinkedHashSet<ProxyHolder> = LinkedHashSet(managedProxies.values)
    private val pendingAcquisitions: Queue<CancellableContinuation<ProxyHolder>> = LinkedList()
    private val random: Random = Random()

    suspend fun acquire(): Proxy = withContext(dispatcher) {
        val holder = selectProxy() ?: suspendCancellableCoroutine(pendingAcquisitions::add)
        availableProxies.remove(holder)
        holder.proxy
    }

    private fun selectProxy(): ProxyHolder? {
        if (availableProxies.isEmpty()) {
            return null
        }
        while (true) {
            for (holder in availableProxies) {
                if (holder.health.score > nextBiasedScore()) {
                    return holder
                }
            }
        }
    }

    private fun nextBiasedScore(): Double {
        return random.nextDouble() * (1.0 - scoreBias)
    }

    fun release(proxy: Proxy, result: ProxyHealth.Result? = null) = scope.launch {
        managedProxies[proxy]?.also { holder ->
            delay(delayBeforeReuse.toMillis())
            result?.also(holder.health::adjust)
            when (val cont = pendingAcquisitions.poll()) {
                null -> availableProxies.add(holder) // Add to available proxies when no coroutine waiting for proxy
                else -> cont.resume(holder) // Resume coroutine waiting in queue for a proxy
            }
        }
    }

    private data class ProxyHolder(val proxy: Proxy, val health: ProxyHealth = ProxyHealth())
}