package io.github.springroll.dl

import io.github.springroll.test.AbstractSpringTest
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class GroovyShellExecutionTest extends AbstractSpringTest {

    @Autowired
    GroovyShellExecution execution

    @Test
    void interactWithBean() {
        assert 1 == execution.execute('1')
    }

}
