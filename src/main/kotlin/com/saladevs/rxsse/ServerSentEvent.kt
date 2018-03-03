package com.saladevs.rxsse

data class ServerSentEvent(val lastId: String,
                           val retry: Long,
                           val event: String,
                           val data: String)
