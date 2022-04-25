package io.github.springroll.test.mysql2h2

import com.granveaud.mysql2h2converter.SQLParserManager
import com.granveaud.mysql2h2converter.converter.H2Converter
import com.granveaud.mysql2h2converter.parser.ParseException
import com.granveaud.mysql2h2converter.sql.SqlStatement
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
    void executeTranslatedScripts() throws IOException, ParseException {
        File h2Script
        for (Resource resource : resources) {
            h2Script = translateToH2Script(resource.getFilename(), resource.getInputStream())
            try {
                jdbcTemplate.execute("runscript from '${h2Script.getCanonicalPath()}'")
                LOGGER.trace('Successfully executed script: {}', h2Script.getCanonicalPath())
            } catch (Exception e) {
                if (ignoreErrors) {
                    LOGGER.debug('Ignore {} error while executing translated script {}, open trace log to see details.', e.getMessage(), h2Script.getCanonicalPath())
                    LOGGER.trace('Execute script error!', e)
                } else {
                    throw e
                }
            } finally {
                h2Script.delete()
            }
        }
    }

    private static File translateToH2Script(String filename, InputStream inputStream) throws IOException, ParseException {
        String namePart = filename.split("\\.")[0]
        filename = filename.replace(namePart, namePart + "_h2_" + System.currentTimeMillis())
        File file = new File(System.getProperty("java.io.tmpdir"), filename)
        StringBuilder h2Sql = new StringBuilder()
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
        Iterator<SqlStatement> sourceIterator = SQLParserManager.parseScript(reader)

        // conversion and execution
        Iterator<SqlStatement> it = H2Converter.convertScript(sourceIterator)
        while (it.hasNext()) {
            SqlStatement st = it.next()
            h2Sql.append(st.toString()).append(";")
        }
        file.write(h2Sql.toString(), StandardCharsets.UTF_8.name())
        return file
    }

}
