package io.github.springroll.web.controller

import groovy.json.JsonSlurper
import io.github.springroll.test.AbstractSpringTest
import org.junit.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MvcResult

class BaseControllerTest extends AbstractSpringTest {

    def c = new BaseController() {}
    def entity = new TestEntity('abc')

    @Test
    void testPostResponse() {
        def result = c.responseOfPost(entity)
        assert result.getStatusCode() == HttpStatus.CREATED
        assert result.getBody() == entity
    }

    @Test
    void testGetResponse() {
        // Get one entity
        def result = c.responseOfGet(entity)
        assert result.getStatusCode() == HttpStatus.OK
        assert result.getBody() == entity

        // Get an entities collection
        result = c.responseOfGet([entity, [c: 3, d: 4]])
        assert result.getStatusCode() == HttpStatus.OK
        assert result.getBody().get(0) == entity

        // Get nothing
        result = c.responseOfGet(null)
        assert result.getStatusCode() == HttpStatus.OK
        assert result.getBody() == null

        result = c.responseOfGet([], new HttpHeaders(['Content-Type': MediaType.TEXT_PLAIN_VALUE]))
        assert result.getStatusCode() == HttpStatus.OK
        assert result.getBody() == []
        assert result.getHeaders().getContentType() == MediaType.TEXT_PLAIN

        def r = get('/web/test-ctrl/emptylist', HttpStatus.OK)
        assert r.getResponse().getContentType() == MediaType.APPLICATION_JSON_UTF8_VALUE
        assert r.getResponse().getContentAsString() == '[]'
    }

    @Test
    void testPutResponse() {
        // Same internal method as responseOfGet
        def result = c.responseOfPut(entity)
        assert result.getStatusCode() == HttpStatus.OK
        assert result.getBody() == entity
    }

    @Test
    void testDeleteResponse() {
        def result = c.responseOfDelete(true)
        assert result.getStatusCode() == HttpStatus.NO_CONTENT
        assert result.getBody() == null

        result = c.responseOfDelete(false)
        assert result.getStatusCode() == HttpStatus.NOT_FOUND
        assert result.getBody() == null
    }

    @Test
    void testWithHeaders() {
        def headers = new HttpHeaders()
        def headerName1 = "Content-Type"
        def headerValue1 = MediaType.APPLICATION_ATOM_XML_VALUE
        def headerName2 = "Content-Length"
        def headerValue2 = "100"
        headers.add(headerName1, headerValue1)
        headers.add(headerName2, headerValue2)
        
        def resWithHeaders = c.responseOfPost(entity, headers)
        assert resWithHeaders.getHeaders().getFirst(headerName1) == headerValue1
        assert resWithHeaders.getHeaders().getFirst(headerName2) == headerValue2
    }

    @Test
    void handleTrouble() {
        def textPlainUtf8 = MediaType.TEXT_PLAIN_VALUE + ';charset=UTF-8'

        def r = get('/web/test-ctrl/trouble/1?div=1', HttpStatus.OK)
        assert r.getResponse().getContentType() == MediaType.APPLICATION_JSON_UTF8_VALUE

        def r1 = get('/web/test-ctrl/trouble/1?div=0', HttpStatus.INTERNAL_SERVER_ERROR)
        assert 'Division by zero' == r1.getResponse().getContentAsString()
        assert r1.getResponse().getContentType() == textPlainUtf8
        def r2 = get('/web/test-ctrl/trouble/1?div=abc', HttpStatus.INTERNAL_SERVER_ERROR)
        assert 'For input string: "abc"' == r2.getResponse().getContentAsString()

        def r3 = get('/web/test-ctrl/trouble/2', HttpStatus.INTERNAL_SERVER_ERROR)
        assert '异常啦' == r3.getResponse().getContentAsString()
        assert r3.getResponse().getContentType() == textPlainUtf8
        assert r3.getResponse().getHeader(BaseController.ResponseHeader.ERROR_TYPE_KEY) == BaseController.ResponseHeader.ERROR_TYPE_VAL_SYS
        get('/web/test-ctrl/trouble/3', HttpStatus.NOT_FOUND)

        MvcResult bussinessErrResult = get('/web/test-ctrl/trouble/4', HttpStatus.INTERNAL_SERVER_ERROR)
        assert bussinessErrResult.getResponse().getContentAsString() == 'empty stack'
        assert bussinessErrResult.getResponse().getHeader(BaseController.ResponseHeader.ERROR_TYPE_KEY) == BaseController.ResponseHeader.ERROR_TYPE_VAL_BIZ
    }

    @Test
    void ignorePropertyInEntity() {
        def r = get('/web/test-ctrl/json/entity', HttpStatus.OK).getResponse().getContentAsString()
        def m = new JsonSlurper().parseText(r)
        assert m.size() == 1
        assert m.containsKey('notIgnored')
        assert !m.containsKey('ignored')
    }

    @Test
    void getDataTrunk() {
        def r = get('/web/test-ctrl/datatrunk', HttpStatus.OK).response.contentAsString
        def dt = new JsonSlurper().parseText(r)
        assert dt.data.size() == 3
        assert dt.total == 10
    }

    class TestEntity {
        TestEntity(String name) {
            this.name = name
        }
        String id
        String name
    }

}
