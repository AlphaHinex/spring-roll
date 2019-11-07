package io.github.springroll.utils.cipher

import io.github.springroll.base.CharacterEncoding
import spock.lang.Specification

class AesSpec extends Specification {

    def key = '7AB0B2F6316C2921'
    def content = '<REQ><HOS_ID><![CDATA[1001]]></HOS_ID></REQ>'

    def "AES encrypt and decrypt (byte[])"() {
        def c = new Aes('ECB', 'PKCS5Padding', key)
        def encrypted = c.encrypt(content.getBytes(CharacterEncoding.getCharset()))
        def decrypted = c.decrypt(encrypted)

        expect:
        assert content.getBytes(CharacterEncoding.getCharset()) == decrypted
    }

}
