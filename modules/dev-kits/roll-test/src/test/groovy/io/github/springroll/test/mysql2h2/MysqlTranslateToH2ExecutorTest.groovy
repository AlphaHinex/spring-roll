package io.github.springroll.test.mysql2h2

import io.github.springroll.test.AbstractSpringTest
import org.junit.Test
import org.springframework.context.annotation.Profile

@Profile('test')
class MysqlTranslateToH2ExecutorTest extends AbstractSpringTest {

    @Test
    void test() {
        assert true
    }

}
