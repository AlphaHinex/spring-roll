package io.github.springroll.test

import groovy.json.JsonSlurper
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractIntegrationTest {

    @LocalServerPort
    protected int port

    protected def getPrefix() {
        "http://localhost:$port"
    }

    protected def get(String url, HttpStatus status) {
        get(url, null, status)
    }

    protected def get(String url, Map<String, List<String>> headers, HttpStatus status) {
        perform('GET', url, headers, null, status)
    }

    // https://stackoverflow.com/questions/25692515/groovy-built-in-rest-http-client
    private def perform(String method, String url, Map<String, List<String>> headers, String data, HttpStatus status) {
        def connection = openConnection(url)
        connection.setRequestMethod(method.toUpperCase())
        headers?.each {entry ->
            entry.value.each {
                connection.setRequestProperty(entry.key, it)
            }
        }
        if (data > '') {
            connection.setDoOutput(true)
            connection.setRequestProperty('Content-Type', 'application/json')
            connection.getOutputStream().write(data.getBytes('UTF-8'))
        }
        int resCode = connection.getResponseCode()
        assert resCode == status.value()
        connection
    }

    private def openConnection(url) {
        if (url.contains('://')) {
            return url.toURL().openConnection()
        } else {
            return "${getPrefix()}$url".toURL().openConnection()
        }
    }

    private static boolean isSuccess(String httpMethod, int actualStatusCode) {
        def successStatus
        switch (httpMethod.toUpperCase()) {
            case 'POST':
                successStatus = HttpStatus.CREATED
                break
            case 'DELETE':
                successStatus = HttpStatus.NO_CONTENT
                break
            default:
                successStatus = HttpStatus.OK
        }
        successStatus.value() == actualStatusCode
    }

    /**
     * check response status of GET url
     * and return response body
     *
     * @param  url
     * @param  status expected HTTP status
     * @return response body
     */
    protected def resOfGet(String url, HttpStatus status) {
        resOfGet(url, null, status)
    }

    protected def resOfGet(String url, Map<String, List<String>> headers, HttpStatus status) {
        def connection = perform('GET', url, headers, null, status)
        def str = isSuccess('GET', connection.getResponseCode()) ? connection.getInputStream().getText() : ''
        try {
            str = new JsonSlurper().parseText(str)
        } catch (ignored) { }
        str
    }

    protected def resOfPost(String url, String data, HttpStatus status) {
        resOfPost(url, null, data, status)
    }

    protected def resOfPost(String url, Map<String, List<String>> headers, String data, HttpStatus status) {
        def connection = post(url, headers, data, status)
        def str = isSuccess('GET', connection.getResponseCode()) ? connection.getInputStream().getText() : ''
        try {
            str = new JsonSlurper().parseText(str)
        } catch (ignored) { }
        str
    }

    protected def post(String url, String data, HttpStatus status) {
        post(url, null, data, status)
    }

    protected def post(String url, Map<String, List<String>> headers, String data, HttpStatus status) {
        perform('POST', url, headers, data, status)
    }

    protected def put(String url, String data, HttpStatus status) {
        put(url, null, data, status)
    }

    protected def put(String url, Map<String, List<String>> headers, String data, HttpStatus status) {
        perform('PUT', url, headers, data, status)
    }

    protected def delete(String url, HttpStatus status) {
        delete(url, null, status)
    }

    protected def delete(String url, Map<String, List<String>> headers, HttpStatus status) {
        perform('DELETE', url, headers, null, status)
    }

    protected def options(String url, HttpStatus status) {
        options(url, null, status)
    }

    protected def options(String url, Map<String, List<String>> headers, HttpStatus status) {
        perform('OPTIONS', url, headers, null, status)
    }

}
