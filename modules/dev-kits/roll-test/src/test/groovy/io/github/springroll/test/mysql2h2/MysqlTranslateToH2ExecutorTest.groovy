package io.github.springroll.test.mysql2h2

import io.github.springroll.test.AbstractSpringTest
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.jdbc.core.JdbcTemplate

class MysqlTranslateToH2ExecutorTest extends AbstractSpringTest {

    @Autowired
    JdbcTemplate jdbcTemplate
    @Autowired
    MysqlTranslateToH2Executor executor

    @Test
    void test() {
        def map = jdbcTemplate.queryForMap('select count(*) from champ_app_mgr_d')
        assert map.size() == 1
        assert map['COUNT(*)'] == 1
    }

    @Test
    void testTranslatingError() {
        def file = new File(File.createTempDir(), 'test.sql')
        file.write('ALTER TABLE `champ_app_mgr_d` DROP COLUMN `ss`')
        Resource sql = new FileSystemResource(file)
        executor.resources = [sql]
        executor.ignoreErrors = true
        executor.executeTranslatedScripts()

        def map = jdbcTemplate.queryForMap('select ss from champ_app_mgr_d')
        assert map.size() == 1
        assert map.containsKey('ss')
    }

}
