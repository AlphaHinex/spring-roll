package io.github.springroll.web

import spock.lang.Specification

class ArtificialHttpServletRequestSpec extends Specification {

    def contextPath = '/context'
    def servletPath = '/servlet/foo/bar'
    def uri = "$contextPath$servletPath"
    def params = [a: ['1'] as String[]]
    def request = new ArtificialHttpServletRequest(contextPath, servletPath, uri, params)

    def 'check useful getters'() {
        expect:
        request.getContextPath() == contextPath
        request.getServletPath() == servletPath
        request.getRequestURI() == uri
        request.getParameter('a') == params.get('a')[0]
    }

    def 'not support methods'() {
        def exclude = ['getContextPath', 'getServletPath', 'getRequestURI', 'getParameter']

        expect:
        request.getClass().getDeclaredMethods().findAll {
            !exclude.contains(it.getName()) && it.getParameterCount() == 0 && it.getReturnType() == String.class
        }.every { method ->
            println "${method.getReturnType().getName()} ${method.getName()} ${method.getParameterCount()}"
            method.invoke(request) == null
        }
    }

}
