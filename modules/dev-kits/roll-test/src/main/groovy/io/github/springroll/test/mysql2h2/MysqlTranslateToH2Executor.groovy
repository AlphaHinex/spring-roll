package io.github.springroll.test.mysql2h2

import com.alibaba.druid.DbType
import com.alibaba.druid.sql.SQLUtils
import com.alibaba.druid.sql.ast.SQLStatement
import com.alibaba.druid.sql.parser.SQLParserFeature
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.io.Resource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import java.nio.charset.StandardCharsets

@Component
@ConditionalOnProperty(name = "roll.test.datasource.type", havingValue = "mysql2h2")
class MysqlTranslateToH2Executor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MysqlTranslateToH2Executor)

    @Value('${roll.test.datasource.mysql-scripts:classpath*:sql/**/*.sql}')
    private Resource[] resources
    @Value('${roll.test.datasource.ignore-errors:true}')
    private boolean ignoreErrors

    private final JdbcTemplate jdbcTemplate

    MysqlTranslateToH2Executor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate
    }

    @PostConstruct
    void executeTranslatedScripts() {
        File h2Script
        resources.toList().sort({ r1, r2 ->
            r1.getURI().toString() <=> r2.getURI().toString()
        }).each { resource ->
            LOGGER.trace("Translating {}", resource.getFilename())
            h2Script = translateToH2Script(resource.getFilename(), resource.getInputStream())
            try {
                jdbcTemplate.execute("runscript from '${h2Script.getCanonicalPath()}'")
                LOGGER.trace('Successfully executed script: {}', h2Script.getCanonicalPath())
            } catch (Exception e) {
                if (ignoreErrors) {
                    LOGGER.debug('Ignore {} error while executing translated script {}', e.getMessage(), h2Script.getCanonicalPath())
                } else {
                    throw e
                }
            } finally {
                h2Script.delete()
            }
        }
    }

    private static File translateToH2Script(String filename, InputStream inputStream) {
        String namePart = filename.split("\\.")[0]
        filename = filename.replace(namePart, namePart + "_h2_" + System.currentTimeMillis())
        File file = new File(System.getProperty("java.io.tmpdir"), filename)

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
        String sql = reader.readLines().join("\r\n")
        List<SQLStatement> stmts = SQLUtils.parseStatements(sql, DbType.mysql, SQLParserFeature.MySQLSupportStandardComment)

        file.write(SQLUtils.toSQLString(stmts, DbType.h2), StandardCharsets.UTF_8.name())
        return file
    }

}
