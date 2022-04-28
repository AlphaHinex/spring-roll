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
        MockHttpServletRequest request = new MockHttpServletRequest('POST', '/api/echo')
        def cusHeaderKey = 'MockUrl'
        def cusHeaderVal = "${getPrefix()}/test/proxy"
        request.addHeader(cusHeaderKey, cusHeaderVal)
        request.setParameters(['a': '1', 'b': '2'])
        request.setContentType(MediaType.APPLICATION_XML_VALUE)
        def content = '<root></root>'
        request.setContent(content.bytes)

        HttpServletResponse response = new MockHttpServletResponse()
        ReverseProxy.proxyPass(request, response, cusHeaderVal, '/api')

        assert response.status == HttpStatus.OK.value()
        assert response.containsHeader(cusHeaderKey)
        assert response.getHeader(cusHeaderKey) == cusHeaderVal
        assert response.contentAsString == content
    }

    @Test
    void forCoverage() {
        MockHttpServletRequest request = new MockHttpServletRequest('OPTIONS', '/api/options')
        def cusHeaderVal = "${getPrefix()}/test/proxy"

        HttpServletResponse response = new MockHttpServletResponse()
        ReverseProxy.proxyPass(request, response, cusHeaderVal, '/api')

        assert response.status == HttpStatus.NO_CONTENT.value()
    }

}
