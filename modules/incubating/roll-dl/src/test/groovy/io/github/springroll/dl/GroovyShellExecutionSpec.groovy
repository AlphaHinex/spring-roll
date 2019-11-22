package io.github.springroll.dl

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

class GroovyShellExecutionSpec extends Specification {

    def static final SCRIPT_FILE_PATH = "${new File('').getAbsolutePath()}/src/test/resources/io/github/springroll/dl/DynamicScript.groovy"
    def shell = new GroovyShellExecution([:])

    @Unroll
    def "Execute groovy script [ #script ] and get result [ #result ]"() {
        def res = ctx == null ? shell.execute(script) : shell.execute(script, ctx)

        expect:
        res == result

        where:
        result             | script                                                                          | ctx
        'HinexHinexHinex'  | '"Hinex"*3'                                                                     | null
        new String('通过'.getBytes('UTF-8'), StandardCharsets.UTF_8) | 'def age = 26; age < 60 ? "通过" : "不通过"' | null
        'ok'               | 'class Foo { def doIt() { "ok" } }; new Foo().doIt()'                           | null
        null               | ''                                                                              | null
        null               | new File(SCRIPT_FILE_PATH)                                                      | null
        true               | 'scriptContext.userId.length() == 3'                                            | [userId: 'abc']
    }

    def 'Return with type. Run twice to get class from cache'() {
        expect:
        2.times {
            shell.execute('"test".length() > 0', Boolean.class) instanceof Boolean
        }
    }

    def 'Handle exception'() {
        when:
        shell.execute((String) null)
        then:
        thrown(GroovyScriptException)

        when:
        shell.execute('invalid script content')
        then:
        def e = thrown(GroovyScriptException)
        e.cause instanceof MissingPropertyException

        when:
        shell.execute('"test".lengthz()')
        then:
        e = thrown(GroovyScriptException)
        e.cause instanceof MissingMethodException

        when:
        shell.execute('http://www.baidu.com')
        then:
        e = thrown(GroovyScriptException)
        e.cause instanceof MultipleCompilationErrorsException
    }

    @Ignore
    def 'justInTime'() {
        while (true) {
            try {
                shell.execute(new File(SCRIPT_FILE_PATH))
            } catch(Throwable t) {
                t.printStackTrace()
            }
            sleep(1000)
        }

        expect:
        true
    }

}
