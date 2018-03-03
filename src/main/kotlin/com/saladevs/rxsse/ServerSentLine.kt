package com.saladevs.rxsse

internal data class ServerSentLine(val field: String,
                                   val value: String) {

    val isBlank = field.isBlank() && value.isBlank()

    companion object {
        private const val DELIMITER = ':'
        const val ID = "id"
        const val EVENT = "event"
        const val DATA = "data"
        const val RETRY = "retry"

        fun from(line: String) = ServerSentLine(
                field = line.substringBefore(DELIMITER, line),
                value = line.substringAfter(DELIMITER, ""))
    }

}
