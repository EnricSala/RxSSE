package com.saladevs.rxsse

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class RxSSE(private val client: OkHttpClient = OkHttpClient()) {

    fun connectTo(url: String) = start(prepare(url))

    fun connectTo(request: Request) = start(prepare(request))

    private fun prepare(url: String) = Request.Builder().url(url)
            .header(ACCEPT_HEADER, SSE_MIME_TYPE).build()

    private fun prepare(request: Request) = request.newBuilder()
            .header(ACCEPT_HEADER, SSE_MIME_TYPE).build()

    private fun start(request: Request): Flowable<ServerSentEvent> =
            linesOf(request)
                    .scan(EventBuilder()) { builder, next -> builder.accept(next) }
                    .filter { it.isReady }
                    .map { it.build() }

    private fun linesOf(request: Request): Flowable<ServerSentLine> =
            Flowable.create({ emitter ->
                val call = client.newCall(request)
                emitter.setCancellable { call.cancel() }
                try {
                    call.execute().use { response ->
                        if (response.isSuccessful) {
                            val source = response.body()!!.source()
                            while (!emitter.isCancelled) {
                                val line = source.readUtf8LineStrict()
                                emitter.onNext(ServerSentLine.from(line))
                            }
                        } else {
                            val error = "HTTP ${response.code()}"
                            emitter.tryOnError(RuntimeException(error))
                        }
                    }
                } catch (ioe: IOException) {
                    emitter.tryOnError(ioe)
                }
            }, BackpressureStrategy.MISSING)

    companion object {
        private const val ACCEPT_HEADER = "Accept"
        private const val SSE_MIME_TYPE = "text/event-stream"
    }

}
