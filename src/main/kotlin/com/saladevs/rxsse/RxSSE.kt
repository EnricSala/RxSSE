package com.saladevs.rxsse

import io.reactivex.Flowable
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.BufferedSource

class RxSSE(private val client: OkHttpClient = OkHttpClient()) {

    fun connectTo(url: String) = start(prepare(url))

    fun connectTo(request: Request) = start(prepare(request))

    private fun prepare(url: String) = Request.Builder().url(url)
            .header(ACCEPT_HEADER, SSE_MIME_TYPE).build()

    private fun prepare(request: Request) = request.newBuilder()
            .header(ACCEPT_HEADER, SSE_MIME_TYPE).build()

    private fun start(request: Request): Flowable<ServerSentEvent> =
            execute(request).flatMapPublisher { events(it) }

    private fun execute(request: Request): Single<BufferedSource> =
            Single.create { emitter ->
                val response = client.newCall(request)
                        .also { emitter.setCancellable { it.cancel() } }
                        .execute()
                if (response.isSuccessful) {
                    val source = response.body()!!.source()
                    emitter.onSuccess(source)
                } else {
                    val error = "HTTP ${response.code()}"
                    emitter.tryOnError(RuntimeException(error))
                }
            }

    private fun events(source: BufferedSource): Flowable<ServerSentEvent> =
            Flowable.using({ source }, { lines(it) }, { it.close() })
                    .scan(EventBuilder()) { builder, next -> builder.accept(next) }
                    .filter { it.isReady }
                    .map { it.build() }

    private fun lines(source: BufferedSource): Flowable<ServerSentLine> =
            Flowable.generate { emitter ->
                val line = source.readUtf8LineStrict()
                emitter.onNext(ServerSentLine.from(line))
            }

    companion object {
        private const val ACCEPT_HEADER = "Accept"
        private const val SSE_MIME_TYPE = "text/event-stream"
    }

}
