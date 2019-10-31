package io.github.springroll.utils.cipher

import io.github.springroll.CharacterEncoding
import spock.lang.Specification

class RsaSpec extends Specification {

    def signAlgo = 'SHA1WithRSA'
    def keySize = 1024
    def content = '<REQ><HOS_ID><![CDATA[1001]]></HOS_ID></REQ>'

    def "RSA encrypt and decrypt (byte[])"() {
        def c = new Rsa(signAlgo)
        Map<String, String> map = c.generateKeyPair(keySize)

        def publicKey = map.publicKey
        def privateKey = map.privateKey

        def encrypted = c.encrypt(content.getBytes(CharacterEncoding.getCharset()), publicKey)
        def decrypted = c.decrypt(encrypted, privateKey)

        expect:
        assert content.getBytes(CharacterEncoding.getCharset()) == decrypted
    }

    def "RSA sign and verify"() {
        def c = new Rsa(signAlgo)
        Map<String, String> map = c.generateKeyPair(keySize)

        def publicKey = map.publicKey
        def privateKey = map.privateKey

        def sign = c.sign(content, privateKey)

        expect:
        c.verifySign(content, sign, publicKey)
    }

    def "Check sign result"() {
        def ctt = 'partner="2088021705112890"&seller_id="zfb_sj@sj-hospital.org"&out_trade_no="hOpTChleZacRtNEpFVQEErZTlyNzwZyU"&subject="挂号费"&body="门诊挂号费用"&total_fee="0.01"&notify_url="http://weixin.propersoft.cn/iosj/api/receiveALIpayNotice"&service="mobile.securitypay.pay"&payment_type="1"&_input_charset="utf-8"&it_b_pay="30m"'
        def priKey = 'MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAOn5Fucgtzqzjuw9+iXuSduRclejkHCr2ibZ/53d1knae+83pA02qLr3VdYvyrPQzx5G2QLD+o0vCXi83+VrSEvNBWYdLGmEGFbt659aBR0l9LzV8LDPI00090oQmwvxNYdZuKqYNaOQ96ldG7Kc9ttpTj+rDiPOPM/JtK5u+RBbAgMBAAECgYEA4LOUR0yoK/weGsw63QrIa9J3matAILUqwX1DfH18O1lVBRzOtNH1ofm/ugAUEDVNdKpd58YHbmfqFMZv0IkD19Jr17J9XCDbQfdgMnVqjymQQKR2hlGjFsJ438dIzySUIKKsYSS5JO2hBki7CslVEsT/N5Xi2ym30Kd/DCTw1CECQQD05o5xrrOgbtRs8GMr9JMm8nC6YPLDYRDNPDMPFqI3U9W/tHF6LeJstCPHD7YrNq4OiVRObQjmI/8/f7nhhcflAkEA9JO/B0dw8lLMX2zJ2Q1yDAbkgDUxBYWU7/C0ceNV3yhtrnaMo+cDeizC3bVaakzC3M2OXHC/fHZauB7Yty1zPwJAGnqrQCNxjJEvKrA7KM+C8Z0ZDy3YUcO2+1nc826xD2ZjIB00f89iQfqgO6+5NKbu3ud+VBR599hAiu8WfAno+QJBAK//AaADUMuPuXGRn06J1l/BNfzsSfJnRd3DoJWzub90IDlDJcFsI5xvSWqxbfbAdhjd8mui1qiBopaL2/c0xZECQEDaaEW9vrdBlTkSeknzFXCOhAhZBWfpttP+eHKrG9l/GeOMFeozN6SqWYip+0cwUcm8OxDvrcMYoF1xwvNNtoc='
        def sign = 'ei7BMWa0cnGmcyEnDwZuNBYquG6Omq/FcWrx/Ft4Be20CvbLvPdC6H+UoNtbl+IhVENipDXkY4b4QMP0wlEfN7QqDyznbJqLRMSb3P+wVaUW21GVMilXCfYE8m8Y7tfse750d2EGSKDnQYmMgHJm+cFE0Lxwnj8AaQfuAoKbvaI='

        def c = new Rsa(signAlgo)
        def s = c.sign(ctt, priKey)

        expect:
        sign == s
    }

}
