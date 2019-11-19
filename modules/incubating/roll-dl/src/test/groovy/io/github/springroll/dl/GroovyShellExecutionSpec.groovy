package io.github.springroll.dl

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

class GroovyShellExecutionSpec extends Specification {

    def shell = new GroovyShellExecution(new Binding())

    @Unroll
    def "Execute groovy script [ #script ] and get result [ #result ]"() {
        def res = shell.execute(script)

        expect:
        res == result

        where:
        result                                                       | script
        'HinexHinexHinex'                                            | '"Hinex"*3'
        new String('通过'.getBytes('UTF-8'), StandardCharsets.UTF_8) | 'def age = 26; age < 60 ? "通过" : "不通过"'
        'ok'                                                         | 'class Foo { def doIt() { "ok" } }; new Foo().doIt()'
    }

    def 'Return with type'() {
        def result = shell.execute('"test".length() > 0', Boolean.class)
        expect:
        result
        result instanceof Boolean
    }

    def 'Handle exception'() {
        when:
        shell.execute(null)
        then:
        def e1 = thrown(GroovyScriptException)
        e1.cause instanceof IllegalArgumentException

        when:
        shell.execute('invalid script content')
        then:
        def e2 = thrown(GroovyScriptException)
        e2.cause instanceof MissingPropertyException

        when:
        shell.execute('"test".lengthz()')
        then:
        def e3 = thrown(GroovyScriptException)
        e3.cause instanceof MissingMethodException

        when:
        shell.execute('http://www.baidu.com')
        then:
        def e4 = thrown(GroovyScriptException)
        e4.cause instanceof MultipleCompilationErrorsException
    }

    @Ignore
    def 'justInTime'() {
        def rootDir = "${new File('').getAbsolutePath()}/src/test/resources"

        while (true) {
            try {
                new File("$rootDir/io/github/springroll/dl/DynamicScript.groovy").withReader {
                    shell.getShell().evaluate(it)
                }
            } catch(Throwable t) {
                t.printStackTrace()
            }
            sleep(1000)
        }

        expect:
        true
    }

}
