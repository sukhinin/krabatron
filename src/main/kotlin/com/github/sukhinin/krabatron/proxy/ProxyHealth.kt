package com.github.sukhinin.krabatron.proxy

class ProxyHealth(historyDepth: Int = 10) {

    companion object {
        private const val SUCCESS_SCORE: Double = 1.0
        private const val FAILURE_SCORE: Double = 0.0
        private const val NEUTRAL_SCORE: Double = (SUCCESS_SCORE - FAILURE_SCORE) / 2
    }

    private val data: DoubleArray = DoubleArray(historyDepth) { NEUTRAL_SCORE }
    private var index: Int = 0

    val score: Double get() = data.sum() / data.size

    fun adjust(result: Result) {
        when (result) {
            Result.SUCCESS -> adjust(SUCCESS_SCORE)
            Result.FAILURE -> adjust(FAILURE_SCORE)
        }
    }

    private fun adjust(score: Double) {
        data[index] = score
        index = (index + 1) % data.size
    }

    enum class Result { SUCCESS, FAILURE }
}