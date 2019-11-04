package io.github.springroll.utils

import spock.lang.Specification
import spock.lang.Unroll

class PinyinUtilSpec extends Specification {

    @Unroll
    def "Pinyin of #input is #result"() {
        expect:
        result == PinyinUtil.quanpin(input)

        where:
        input       | result
        ''          | ''
        null        | ''
        '好似'       | 'haosi'
        '似的'       | 'side'
        '混1a搭'     | 'hun1ada'
        '绿'         | 'lv'
    }

}
