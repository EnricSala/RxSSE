RxSSE
=====

[Server-Sent Events][sse] client for Android and Java applications.

Built with [Kotlin][kotlin], [RxJava][rxjava] and [OkHttp][okhttp].


Usage
-----

Create an `RxSSE` instance with a default client:

```kotlin
val rxsse = RxSSE()
```

Or with a pre-configured client:

```kotlin
val client = OkHttpClient.Builder().build()
val rxsse = RxSSE(client)
```

Then use it to connect to URLs:

```kotlin
rxsse
    .connectTo("https://localhost/events")
    .onBackpressureLatest()
    .subscribeOn(Schedulers.io(), false)
    .subscribe { Log.i(TAG, "Received: $it") }
```

Notice that the returned `Flowable` does not define a backpressure strategy.
You should define a strategy in order to prevent `MissingBackpressure` errors.
In this case `onBackpressureLatest()` is used, but there are others.

You can also use it with customized requests:

```kotlin
val request = Request.Builder().url(myUrl).build()
rxsse
    .connectTo(request)
    .onBackpressureLatest()
    .subscribeOn(Schedulers.io(), false)
    .subscribe { Log.i(TAG, "Received: $it") }
```

You can use RxJava's `retry` operators to reconnect if the connection
is lost or there is an error.

Attempt to reconnect with a fixed timeout:

```kotlin
rxsse
    .connectTo("https://localhost/events")
    .onBackpressureLatest()
    .retryWhen { it.flatMap { Flowable.timer(5, TimeUnit.SECONDS) } }
    .subscribeOn(Schedulers.io(), false)
    .subscribe { Log.i(TAG, "Received: $it") }
```

Finally, don't forget to unsubscribe when you are done to close the connection:

```kotlin
val disposable = rxsse
    .connectTo("https://localhost/events")
    .onBackpressureLatest()
    .subscribeOn(Schedulers.io(), false)
    .subscribe { Log.i(TAG, "Received: $it") }

// Close the connection
disposable.dispose()
```


Download
--------

Available on JCenter with:

 * Gradle:

```groovy
compile 'com.saladevs:rxsse:0.1.1'
```

 * Maven:

```xml
<dependency>
  <groupId>com.saladevs</groupId>
  <artifactId>rxsse</artifactId>
  <version>0.1.1</version>
</dependency>
```


TODO
----

 * Add tests.

 * Implement automatic retries.

 * Automatically send `Last-Event-ID` header on retries.


Contributing
------------

Contributions and feedback are welcome!

Please create an issue to discuss it first :)


License
-------

    Copyright 2017 Enric Sala

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


 [sse]: https://en.wikipedia.org/wiki/Server-sent_events
 [kotlin]: https://kotlinlang.org/
 [rxjava]: https://github.com/ReactiveX/RxJava
 [okhttp]: https://github.com/square/okhttp
