package io.github.springroll.web

import io.github.springroll.utils.JsonUtil
import io.github.springroll.web.request.ArtificialHttpServletRequest
import org.springframework.http.MediaType
import spock.lang.Specification
import spock.lang.Unroll

class ArtificialHttpServletRequestSpec extends Specification {

    def contextPath = '/context'
    def servletPath = '/servlet/foo/bar'
    def uri = "$contextPath$servletPath"
    def params = [a: ['1'] as String[], c: [] as String[]]
    def request = new ArtificialHttpServletRequest(contextPath, servletPath, uri, params)

    def 'check useful getters'() {
        expect:
        request.getContextPath() == contextPath
        request.getServletPath() == servletPath
        request.getRequestURI() == uri
        request.getParameter('a') == params.get('a')[0]
        request.getParameter('b') == null
        request.getParameter('c') == null
        request.getMethod() == 'GET'
        request.getParameterValues('a') == params.get('a')
        request.getParameterNames().toList() == ['a', 'c']
        request.getContentLength() == -1
        request.getInputStream().available() == 0
    }

    def 'set then get'() {
        request.setMethod('POST')
        request.setCharacterEncoding('utf-8')
        request.setContentType(MediaType.APPLICATION_JSON_VALUE)
        def content = JsonUtil.toJsonIgnoreException([a:1, b:2]).getBytes()
        request.setContent(content)

        expect:
        request.getMethod() == 'POST'
        request.getContentType() == MediaType.APPLICATION_JSON_VALUE
        request.getContentLength() == request.getContentLengthLong()
        request.getContentLength() == content.length
        request.getInputStream().available() == content.length
        request.getInputStream().getBytes() == content
        request.getHeaderNames().hasMoreElements()
        request.getHeader('Content-Type') != null
        request.getHeader('Not-Exist') == null
        request.getHeaders('Content-Type') != null
        !request.getHeaders('Not-Exist').hasMoreElements()
    }

    @Unroll
    def "Set content type #type and get #result"() {
        request.setContentType(type)

        expect:
        request.getContentType() == result
        request.getCharacterEncoding() == encoding

        where:
        type                             | result                           | encoding
        null                             | null                             | null
        ''                               | ''                               | null
        'charset=utf-8'                  | 'charset=utf-8'                  | 'utf-8'
        'application/json;charset=UTF-8' | 'application/json;charset=UTF-8' | 'UTF-8'
        'application/json'               | 'application/json'               | null
    }

    def 'not support methods'() {
        def exclude = [
                'getContextPath',
                'getServletPath',
                'getRequestURI',
                'getParameter',
                'getMethod',
                'setMethod',
                'getParameterValues',
                'getParameterNames',
                'setContentType',
                'updateContentTypeHeader',
                'doAddHeaderValue',
                'getContentType',
                'getContentLength',
                'getContentLengthLong',
                'getInputStream',
                'setContent',
                'getHeader',
                'getHeaders',
                'getHeaderNames',
                'getCharacterEncoding',
                '$jacocoInit'
        ]

        expect:
        request.getClass().getDeclaredMethods().findAll {
            !exclude.contains(it.getName())
        }.every { method ->
            def count = method.getParameterCount()
            Object[] params = new Object[count]
            if (count == 1 && method.getParameterTypes()[0].getSimpleName() == 'boolean') {
                Arrays.fill(params, false)
            } else {
                Arrays.fill(params, null)
            }
            switch (method.getReturnType().getSimpleName()) {
                case 'boolean':
                    method.invoke(request, params) == false
                    break
                case 'long':
                case 'int':
                    method.invoke(request, params) == 0
                    break
                default:
                    method.invoke(request, params) == null
            }
        }
    }

}
