package com.saladevs.rxsse

internal class EventBuilder(private val lastId: String = DEFAULT_ID,
                            private val buffer: List<ServerSentLine> = listOf()) {

    val isReady = buffer.size > 1 && buffer.last().isBlank

    fun accept(line: ServerSentLine): EventBuilder = when {
        line.isBlank && buffer.isEmpty() -> this
        isReady -> EventBuilder(lastId, listOf(line))
        else -> EventBuilder(lastId, buffer + line)
    }

    fun build(): ServerSentEvent =
            if (isReady) ServerSentEvent(findId(), findRetry(), findEvent(), findData())
            else throw IllegalStateException("builder not ready")

    private fun findId(): String = buffer
            .firstOrNull { ServerSentLine.ID == it.field }
            ?.value ?: lastId

    private fun findRetry(): Long = buffer
            .firstOrNull { ServerSentLine.RETRY == it.field }
            ?.value?.toLong() ?: DEFAULT_RETRY

    private fun findEvent(): String = buffer
            .firstOrNull { ServerSentLine.EVENT == it.field }
            ?.value ?: DEFAULT_EVENT

    private fun findData(): String = buffer
            .filter { ServerSentLine.DATA == it.field }
            .joinToString(System.lineSeparator()) { it.value }

    companion object {
        private const val DEFAULT_ID = ""
        private const val DEFAULT_EVENT = ""
        private const val DEFAULT_RETRY = 3000L
    }

}
