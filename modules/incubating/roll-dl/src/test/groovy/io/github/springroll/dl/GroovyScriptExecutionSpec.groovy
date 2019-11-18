package io.github.springroll.dl

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

class GroovyScriptExecutionSpec extends Specification {

    @Unroll
    def "Execute groovy script [ #script ] and get result [ #result ]"() {
        def res = GroovyScriptExecution.execute(script)

        expect:
        res == result

        where:
        result                                                       | script
        'HinexHinexHinex'                                            | '"Hinex"*3'
        new String('通过'.getBytes('UTF-8'), StandardCharsets.UTF_8) | 'def age = 26; age < 60 ? "通过" : "不通过"'
    }

    def 'Return with type'() {
        def result = GroovyScriptExecution.execute('"test".length() > 0', Boolean.class)
        expect:
        result
        result instanceof Boolean
    }

    def 'Handle exception'() {
        when:
        GroovyScriptExecution.execute(null)
        then:
        def e1 = thrown(ScriptException)
        e1.cause instanceof IllegalArgumentException

        when:
        GroovyScriptExecution.execute('invalid script content')
        then:
        def e2 = thrown(ScriptException)
        e2.cause instanceof MissingPropertyException

        when:
        GroovyScriptExecution.execute('"test".lengthz()')
        then:
        def e3 = thrown(ScriptException)
        e3.cause instanceof MissingMethodException

        when:
        GroovyScriptExecution.execute('http://www.baidu.com')
        then:
        def e4 = thrown(ScriptException)
        e4.cause instanceof MultipleCompilationErrorsException
    }

}
