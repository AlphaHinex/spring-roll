package io.github.springroll.web.request

import io.github.springroll.test.AbstractSpringTest
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest

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

    @Test(expected = ServletException)
    void noAdapterForHandler() {
        overrideSingleton('requestMappingHandlerAdapter', new AlwaysFalseRequestMappingHandlerAdapter())
        def request = new ArtificialHttpServletRequest('', '', '/service/handler/mapping')
        invokeControllerByRequest.invoke(request)
    }

}

@Component
class WithoutHandlerRequestMappingHandlerMapping extends RequestMappingHandlerMapping {
    @Override
    protected HandlerMethod getHandlerInternal(HttpServletRequest request) throws Exception {
        request.getRequestURI().contains('/service/handler/mapping') ? new HandlerMethod('', 'toString') : null
    }
}

@Component
class AlwaysFalseRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {
    @Override
    protected boolean supportsInternal(HandlerMethod handlerMethod) {
        false
    }
}
