package io.github.springroll.utils.digest

import spock.lang.Specification
import spock.lang.Unroll

class Md5Spec extends Specification {

    @Unroll
    def "MD5 HEX string of #input is #result"() {
        expect:
        result == Md5.md5Hex(input)

        where:
        result                              | input
        'e10adc3949ba59abbe56e057f20f883e'  | '123456'
    }

}
