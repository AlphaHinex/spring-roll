package io.github.springroll.base

import spock.lang.Specification

class VersionSpec extends Specification {

    def 'test'() {
        expect:
        Version.HASH == Version.getVersion().hashCode()
    }

}
