package io.github.springroll.test

import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.transaction.annotation.Transactional

@Transactional
@SqlConfig(encoding = "UTF-8")
abstract class AbstractSpringTxTest extends AbstractSpringTest {
}
