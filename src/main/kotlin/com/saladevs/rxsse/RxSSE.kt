package com.saladevs.rxsse

import io.reactivex.Flowable
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.Request

class RxSSE(private val client: OkHttpClient = OkHttpClient()) {

    fun connectTo(url: String) = start(prepare(url))

    fun connectTo(request: Request) = start(prepare(request))

    private fun prepare(url: String) = Request.Builder().url(url)
            .header(ACCEPT_HEADER, SSE_MIME_TYPE).build()

    private fun prepare(request: Request) = request.newBuilder()
            .header(ACCEPT_HEADER, SSE_MIME_TYPE).build()

    private fun start(request: Request): Flowable<ServerSentEvent> =
            execute(request).flatMapPublisher { it.events() }

    private fun execute(request: Request): Single<Connection> =
            Single.create { emitter ->
                val call = client.newCall(request)
                emitter.setCancellable { call.cancel() }
                try {
                    val response = call.execute()
                    if (response.isSuccessful) {
                        emitter.onSuccess(Connection(call, response))
                    } else {
                        val error = "HTTP ${response.code()}"
                        emitter.tryOnError(RuntimeException(error))
                    }
                } catch (t: Throwable) {
                    emitter.tryOnError(t)
                }
            }

    companion object {
        private const val ACCEPT_HEADER = "Accept"
        private const val SSE_MIME_TYPE = "text/event-stream"
    }

}
