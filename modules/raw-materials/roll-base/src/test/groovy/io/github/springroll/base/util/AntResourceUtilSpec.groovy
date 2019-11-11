package io.github.springroll.base.util

import spock.lang.Specification

class AntResourceUtilSpec extends Specification {

    def "Support multi-locations separated by comma"() {
        def res = AntResourceUtil.getResources('classpath*:**/ant1.test,classpath*:**/springroll/**/*.test')

        expect:
        res.length == 2 // According to actual files under path pattern
    }

}
