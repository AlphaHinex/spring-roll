package io.github.springroll.dl

import io.github.springroll.test.AbstractSpringTest
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class GroovyShellExecutionTest extends AbstractSpringTest {

    @Autowired
    GroovyShellExecution execution

    @Test
    void interactWithBean() {
        def check = 'applicationContext.scriptableBean.getInterval() < 60 && applicationContext.scriptableBean.isFixed()'
        assert !execution.execute(check)

        def script = """
            applicationContext.scriptableBean.setInterval(30)
            applicationContext.scriptableBean.setFixed(true)
            $check
"""
        assert execution.execute(script)
    }

    @Test(expected = GroovyScriptException)
    void couldNotGetInvisibleBeanDirectly() {
        execution.execute('applicationContext.invisibleForShell.shouldNotBeInvokedInShell()')
    }

    @Test
    void invisibleBeanWorkAround() {
        def script = """
            def bean = io.github.springroll.web.ApplicationContextHolder.getBean('invisibleForShell')
            bean.shouldNotBeInvokedInShell()
"""
        execution.execute(script)
    }

}
