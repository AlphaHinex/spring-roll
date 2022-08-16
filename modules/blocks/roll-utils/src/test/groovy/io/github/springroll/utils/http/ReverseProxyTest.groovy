package io.github.springroll.utils.http

import io.github.springroll.test.AbstractIntegrationTest
import org.junit.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

import javax.servlet.http.HttpServletResponse

class ReverseProxyTest extends AbstractIntegrationTest {

    @Test
    void checkFunction() {
        // redirect /echo/api/echo to ${getPrefix()}/test/proxy/echo
        MockHttpServletRequest request = new MockHttpServletRequest('POST', '/echo/api/echo')
        def reqHeader = 'MockUrl'
        def proxyUrl = "${getPrefix()}/test/proxy"
        request.addHeader(reqHeader, proxyUrl)
        request.setParameters(['a': '1', 'b': '2'])
        request.setContentType(MediaType.APPLICATION_XML_VALUE)
        request.setContextPath('/echo')
        def content = '<root></root>'
        request.setContent(content.bytes)

        HttpServletResponse response = new MockHttpServletResponse()
        ReverseProxy.proxyPass(request, response, proxyUrl, '/api', [customHeader: 'customValue'])

        assert response.status == HttpStatus.OK.value()
        assert response.containsHeader(reqHeader)
        assert response.containsHeader('customHeader')
        assert response.getHeader('customHeader') == 'customValue'
        assert response.getHeader(reqHeader) == proxyUrl
        assert response.contentAsString == content
    }

    @Test
    void forCoverage() {
        MockHttpServletRequest request = new MockHttpServletRequest('OPTIONS', '/options')
        def cusHeaderVal = "${getPrefix()}/test/proxy"

        HttpServletResponse response = new MockHttpServletResponse()
        ReverseProxy.proxyPass(request, response, cusHeaderVal, '', [:])
        ReverseProxy.proxyPass(request, response, cusHeaderVal, '')

        assert response.status == HttpStatus.NO_CONTENT.value()
    }

}
