package io.github.springroll.web

import io.github.springroll.test.AbstractSpringTest
import io.github.springroll.web.utils.SpElParser
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

class ApplicationContextHolderTest extends AbstractSpringTest {

    @Autowired
    private SpElParser parser

    @Autowired
    private TestController controller

    @Test
    void cover() {
        ApplicationContext context = ApplicationContextHolder.getApplicationContext()
        def beanName = 'testController'
        assert context.getBean(beanName) == ApplicationContextHolder.getBean(beanName)
        assert context.getBean(TestController.class) == ApplicationContextHolder.getBean(TestController.class)
        assert context.getBean(beanName, TestController.class) == ApplicationContextHolder.getBean(beanName, TestController.class)
    }

    @Test
    void invokeStaticMethod() {
        def spEl = '#{T(io.github.springroll.web.ApplicationContextHolder).getBean(T(io.github.springroll.web.TestController)).charset}'
        assert parser.parse(spEl) == controller.getCharset()
    }

}
