interface RequestExecutor<T> {
    suspend fun get(request: Request): T
}
