package io.github.springroll.utils.http

import okhttp3.MediaType
import okhttp3.Response
import spock.lang.Ignore
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class HttpClientSpec extends Specification {

    def static final TEAMCITY = 'https://teamcity.jetbrains.com'

    def "Using all http methods"() {
        def url = "$TEAMCITY/login.html"
        def data = '{"user":"123"}'
        def headers = ['h1': 'header1', 'h2': 'header2']

        expect:
        HttpClient.post(url, MediaType.get('application/x-www-form-urlencoded'), data).code() == 200
        HttpClient.post(url, headers, MediaType.get('application/x-www-form-urlencoded'), data).code() == 200
        HttpClient.put(url, MediaType.get('application/json'), data).code() == 405
        HttpClient.put(url, MediaType.get('application/json'), data).code() == 405
        HttpClient.get(url).code() == 200
        HttpClient.get(url, headers).code() == 200
        HttpClient.delete(url, MediaType.get('application/x-www-form-urlencoded'), data).code() == 405
        HttpClient.delete(url, headers, MediaType.get('application/x-www-form-urlencoded'), data).code() == 405
        HttpClient.delete(url).code() == 405
        HttpClient.delete(url, headers).code() == 405
    }

    def "Could get stream"() {
        def r = HttpClient.get("$TEAMCITY/img/collapse.png")

        expect:
        r.body() != null
        r.headers().get('Content-Type') == 'image/png'
    }

    def "Async request with callback"() {
        CountDownLatch latch = new CountDownLatch(3)

        def cb = new Callback() {
            @Override
            void onSuccess(Response response) {
                println 'success'
                println response
                latch.countDown()
            }

            @Override
            void onError(IOException ioe) {
                println 'error'
                println ioe
                latch.countDown()
            }
        }

        expect:
        HttpClient.post("$TEAMCITY/login.html", MediaType.get('application/x-www-form-urlencoded'), '{"user":"123"}', cb)
        HttpClient.post('https://www.google.com', MediaType.get('application/x-www-form-urlencoded'), '{"user":"123"}', 200, cb)
        HttpClient.get('http://localhost:9090/pep', 200, cb)
        latch.await(5, TimeUnit.SECONDS)
    }

    @Ignore
    def "test connectException"() {
        def url = "http://localhost:9090/pep"
        def data = '{"user":"123"}'
        def headers = ['h1': 'header1', 'h2': 'header2']

        when:
        def getStart = new Date().getTime()
        HttpClient.get(url, headers)
        then:
        thrown(ConnectException)
        def getSpendTime = new Date().getTime() - getStart

        when:
        getStart = new Date().getTime()
        HttpClient.get(url, headers, 1)
        then:
        thrown(ConnectException)
        assert getSpendTime < new Date().getTime() - getStart

        when:
        def postStart = new Date().getTime()
        HttpClient.post(url, headers, MediaType.get('application/x-www-form-urlencoded'), data)
        then:
        thrown(ConnectException)
        def postSpendTime = new Date().getTime() - postStart

        when:
        postStart = new Date().getTime()
        HttpClient.post(url, headers, MediaType.get('application/x-www-form-urlencoded'), data, 1)
        then:
        thrown(ConnectException)
        assert postSpendTime < new Date().getTime() - postStart

        when:
        def putStart = new Date().getTime()
        HttpClient.put(url, headers, MediaType.get('application/x-www-form-urlencoded'), data)
        then:
        thrown(ConnectException)
        def putSpendTime = new Date().getTime() - putStart

        when:
        putStart = new Date().getTime()
        HttpClient.put(url, headers, MediaType.get('application/x-www-form-urlencoded'), data, 1)
        then:
        thrown(ConnectException)
        assert putSpendTime < new Date().getTime() - putStart

        when:
        def deleteStart = new Date().getTime()
        HttpClient.delete(url, headers, MediaType.get('application/x-www-form-urlencoded'), data)
        then:
        thrown(ConnectException)
        def deleteSpendTime = new Date().getTime() - deleteStart

        when:
        deleteStart = new Date().getTime()
        HttpClient.delete(url, headers, MediaType.get('application/x-www-form-urlencoded'), data, 1)
        then:
        thrown(ConnectException)
        assert deleteSpendTime < new Date().getTime() - deleteStart

    }

}
