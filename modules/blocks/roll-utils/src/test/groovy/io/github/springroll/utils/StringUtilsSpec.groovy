package io.github.springroll.utils

import spock.lang.Specification
import spock.lang.Unroll

class StringUtilsSpec extends Specification {

    @Unroll
    def "Camel case '#input' to snake is #result"() {
        expect:
        result == StringUtils.camelToSnake(input)
        where:
        input      | result
        'fooBar' | 'foo_bar'
        'BothUpper'   | 'both_upper'
    }

}
