package io.github.springroll.dl

import io.github.springroll.utils.CollectionUtil
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

class GroovyShellExecutionSpec extends Specification {

    def static final SCRIPT_FILE_PATH = "${new File('').getAbsolutePath()}/src/test/resources/io/github/springroll/dl/DynamicScript.groovy"
    def execution = new GroovyShellExecution([:])

    @Unroll
    def "Execute groovy script [ #script ] and get result [ #result ]"() {
        def res = ctx == null ? execution.execute(script) : execution.execute(script, ctx)

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
            execution.execute('"test".length() > 0', Boolean.class) instanceof Boolean
        }
    }

    def 'Handle exception'() {
        when:
        execution.execute((String) null)
        then:
        thrown(GroovyScriptException)

        when:
        execution.execute('invalid script content')
        then:
        def e = thrown(GroovyScriptException)
        e.cause instanceof MissingPropertyException

        when:
        execution.execute('"test".lengthz()')
        then:
        e = thrown(GroovyScriptException)
        e.cause instanceof MissingMethodException

        when:
        execution.execute('http://www.baidu.com')
        then:
        e = thrown(GroovyScriptException)
        e.cause instanceof MultipleCompilationErrorsException
    }

    def 'Check script context'() {
        expect:
        execution.execute('scriptContext.a * scriptContext.b', [a:3, b:5]) == 15
        execution.execute('scriptContext.a * scriptContext.b', [a:4, b:6]) == 24

        when:
        execution.execute('scriptContext.a * scriptContext.b')
        then:
        def e = thrown(GroovyScriptException)
        e.cause instanceof NullPointerException
    }

    def data = [
            [id: 0, interval: 30, baseInfo: true],
            [id: 1, interval: 50, baseInfo: false],
            [id: 2, interval: 80, baseInfo: false],
            [id: 3, interval: 90, baseInfo: true],
            [id: 4, interval: 40, baseInfo: true]
    ]

    def rules = [
            'scriptContext.data.interval < 60',
            'scriptContext.data.baseInfo'
    ]

    def batRules = [
            'scriptContext.data.findAll { it.interval < 60 }',
            'scriptContext.data.findAll { it.interval < 60 && it.baseInfo }',
            'scriptContext.data.findAll { it.interval > 85 == it.baseInfo }'
    ]

    def 'singleDataSingleRule'() {
        // 关注校验结果是否通过
        expect:
        execution.execute(rules[0], [data: data[0]], Boolean.class)
    }

    def 'singleDataMultiRules'() {
        // 关注哪条规则没通过
        def notPassed = []
        rules.each { rule ->
            if (!execution.execute(rule, [data: data[1]], Boolean.class)) {
                notPassed << rule
            }
        }

        expect:
        notPassed[0] == rules[1]
    }

    def 'multiDataSingleRule'() {
        // 关注哪些数据没通过
        List result = execution.execute(batRules[1], [data: data], List.class)

        expect:
        result.size() == 2
        result[0] == data[0]
        result[1] == data[4]
    }

    def 'multiDataMultiRules'() {
        // 一批数据要执行相同的一批规则，关注哪些数据没通过。数据顺序或并行执行规则，结果取并集
        def result = execution.executeParallel(batRules.toArray(new String[0]), [data: data])

        expect:
        CollectionUtil.equalCollections(result, data)
    }

    @Ignore
    def 'justInTime'() {
        while (true) {
            try {
                execution.execute(new File(SCRIPT_FILE_PATH))
            } catch(Throwable t) {
                t.printStackTrace()
            }
            sleep(1000)
        }

        expect:
        true
    }

}
