package io.github.springroll.web

import spock.lang.Specification

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
        request.setMethod('POST')
        request.getMethod() == 'POST'
        request.getParameterValues('a') == params.get('a')
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
