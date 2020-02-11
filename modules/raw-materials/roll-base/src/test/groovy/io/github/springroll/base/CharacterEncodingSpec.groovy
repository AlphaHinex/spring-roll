package io.github.springroll.base

import spock.lang.Specification

import java.nio.charset.StandardCharsets

class CharacterEncodingSpec extends Specification {

    def 'test'() {
        expect:
        CharacterEncoding.DEFAULT_ENCODE_NAME == URLEncoder.dfltEncName
        CharacterEncoding.getCharset() == StandardCharsets.UTF_8
        CharacterEncoding.setCharset(StandardCharsets.ISO_8859_1)
        CharacterEncoding.getCharset() == StandardCharsets.ISO_8859_1
    }

}
