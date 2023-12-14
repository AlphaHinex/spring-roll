package io.github.springroll.test.mysql2h2

import io.github.springroll.test.AbstractSpringTest
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

class MysqlTranslateToH2ExecutorTest extends AbstractSpringTest {

    @Autowired
    JdbcTemplate jdbcTemplate

    @Test
    void test() {
        def map = jdbcTemplate.queryForMap('select count(*) from champ_app_mgr_d')
        assert map.size() == 1
        assert map['COUNT(*)'] == 0
    }

}
