package com.saladevs.rxsse

import io.reactivex.Flowable
import okhttp3.Call
import okhttp3.Response

internal class Connection(private val call: Call, response: Response) {

    private val source = response.body()?.source()
            ?: throw IllegalStateException("response body not available")

    private fun line() = ServerSentLine.from(source.readUtf8LineStrict())

    private fun lines(): Flowable<ServerSentLine> =
            Flowable.generate<ServerSentLine> { it.onNext(line()) }
                    .doOnCancel { call.cancel() }

    fun events(): Flowable<ServerSentEvent> = lines()
            .scan(EventBuilder()) { acc, next -> acc.accept(next) }
            .filter { it.isReady }
            .map { it.build() }

}
