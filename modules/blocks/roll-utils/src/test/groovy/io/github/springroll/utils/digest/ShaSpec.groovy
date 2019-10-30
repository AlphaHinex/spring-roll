package io.github.springroll.utils.digest

import spock.lang.Specification
import spock.lang.Unroll

class ShaSpec extends Specification {

    @Unroll
    def "#content after sha256 is #result"() {
        expect:
        result == Sha.sha256(content)

        where:
        result                                                             | content
        '6ca13d52ca70c883e0f0bb101e425a89e8624de51db2d2392593af6a84118090' | 'abc123'
    }

}
