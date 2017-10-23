package com.saladevs.rxsse

data class ServerSentEvent(val lastId: String,
                           val event: String,
                           val data: String)
