package io.github.springroll.web.request

import io.github.springroll.test.AbstractSpringTest
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.method.HandlerMethod

import javax.servlet.http.HttpServletRequest

class HandlerMethodHolderTest extends AbstractSpringTest {

    @Autowired
    private HandlerMethodHolder holder

    @Test
    void test() {
        HttpServletRequest request = new MockHttpServletRequest('GET', '/web/test-ctrl/emptylist')
        HandlerMethod method = holder.getHandlerMethod(request).get()
        assert method.hasMethodAnnotation(GetMapping)

        assert holder.getHandlerMethod(new MockHttpServletRequest('GET','/not-exist')) == Optional.empty()
    }

}
