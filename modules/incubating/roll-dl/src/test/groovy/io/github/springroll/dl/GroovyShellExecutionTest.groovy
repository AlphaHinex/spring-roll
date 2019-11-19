package io.github.springroll.dl

import io.github.springroll.test.AbstractSpringTest
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class GroovyShellExecutionTest extends AbstractSpringTest {

    @Autowired
    GroovyShellExecution execution

    @Test
    void interactWithBean() {
        def check = 'scriptableBean.getInterval() < 60 && scriptableBean.isFixed()'
        assert !execution.execute(check)

        def script = """
            scriptableBean.setInterval(30)
            scriptableBean.setFixed(true)
            $check
"""
        assert execution.execute(script)
    }

}
