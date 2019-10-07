package io.github.springroll.web.utils

import io.github.springroll.test.AbstractSpringTest
import io.github.springroll.web.TestController
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class SpELParserTest extends AbstractSpringTest {

    @Autowired
    SpElParser parser

    @Autowired
    TestController controller

    @Test
    void getBeanInSpEL() {
        mockUser('id', 'name', 'pwdInSpEl')

        String tpl = '{usergroup: {$in: [#{@testController.charset}]}}'
        def val = parser.parse(tpl)

        assert val == "{usergroup: {\$in: [${controller.getCharset()}]}}".toString()
        assert parser.parse('#{@testController.charset}')
    }

    @Test
    void notUseExpressionTemplate() {
        String spEL = """{
'M149', //abc
'0187', // def, ghi
'0010' // zzz
}[1] == #id ? 'a' : 'b'"""
        assert parser.parse(spEL, ["id": '0187'], false) == 'a'
    }

    @Test
    void booleanExp() {
        String spEL = """{
'M149', //abc
'0187', // def, ghi
'0010' // zzz
}[1] == #id"""
        assert parser.parse(spEL, ["id": '0187'], Boolean.class)
    }

}
