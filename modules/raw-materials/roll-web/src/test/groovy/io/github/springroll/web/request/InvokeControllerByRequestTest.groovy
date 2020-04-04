package io.github.springroll.web.request

import io.github.springroll.test.AbstractSpringTest
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity

class InvokeControllerByRequestTest extends AbstractSpringTest {

    @Autowired
    InvokeControllerByRequest invokeControllerByRequest

    @Test(expected = IllegalArgumentException)
    void testRequestCouldNotMappingController() {
        def request = new ArtificialHttpServletRequest('', '', '/path/without/controller')
        invokeControllerByRequest.invoke(request)
    }

    @Test
    void invokeGet() {
        def request = new ArtificialHttpServletRequest('', '', '/web/test-ctrl/emptylist')
        def res = invokeControllerByRequest.invoke(request)
        assert res instanceof ResponseEntity
    }

}
