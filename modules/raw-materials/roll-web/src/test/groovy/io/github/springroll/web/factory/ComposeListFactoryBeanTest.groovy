package io.github.springroll.web.factory

import io.github.springroll.test.AbstractSpringTest
import org.junit.Test

class ComposeListFactoryBeanTest extends AbstractSpringTest {

    @Test
    void createInstanceWithBlankOrNullPattern() {
        checkEmptyList(new ComposeListFactoryBean(null))
        checkEmptyList(new ComposeListFactoryBean(''))
        checkEmptyList(new ComposeListFactoryBean(' '))
        checkEmptyList(new ComposeListFactoryBean('notExist'))
    }

    def checkEmptyList(factory) {
        assert factory.getObjectType() == List.class
        def instance = factory.createInstance()
        assert instance != null && instance.size() == 0
    }

    @Test
    void createInstance() {
        registerListBean()
        def factory = new ComposeListFactoryBean('clfb\\d+')
        assert factory.createInstance().size() == 6
    }

    def registerListBean() {
        overrideSingleton('clfb1', ['a','b','c'])
        overrideSingleton('clfb2', ['d','e','f'])
        overrideSingleton('clfb', ['g','h'])
        overrideSingleton('clfbxx', ['i'])
    }

}
