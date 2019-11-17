package io.github.springroll.dl

import spock.lang.Specification

class DynamicLanguageExecutionSpec extends Specification {

    def "execute groovy script"() {
        def script = '"Hinex"*3'

        expect:
        DynamicLanguageExecution.execute(script) == 'HinexHinexHinex'
    }

}
