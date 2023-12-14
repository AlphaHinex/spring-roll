package io.github.springroll.test.mysql2h2

import io.github.springroll.test.AbstractSpringTest
import org.junit.Test
import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = [
    'roll.test.datasource.type=mysql2h2',
    'roll.test.datasource.ignore-errors=false'
])
class MysqlTranslateToH2ExecutorTest extends AbstractSpringTest {

    @Test
    void test() {
        assert true
    }

}
