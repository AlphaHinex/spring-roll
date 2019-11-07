package io.github.springroll.web

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.springroll.web.controller.BaseController
import io.github.springroll.web.exception.ErrMsgException
import io.github.springroll.web.model.DataTrunk
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.NoHandlerFoundException

import javax.servlet.ServletException

@RestController
@RequestMapping('/web/test-ctrl')
class TestController extends BaseController {

    private String charset = 'UTF-8'

    String getCharset() {
        return charset
    }

    @GetMapping('/emptylist')
    ResponseEntity<List> getAgreement() {
        return responseOfGet([])
    }

    @GetMapping(path = "/trouble/1", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, String>> test1(@RequestParam String div) {
        return new ResponseEntity<String>(['result': 10 / Integer.parseInt(div) + ""], HttpStatus.OK);
    }

    @GetMapping("/trouble/2")
    ResponseEntity<String> test2() {
        throw new ServletException('异常啦')
    }

    @GetMapping("/trouble/3")
    ResponseEntity<String> test3() {
        throw new NoHandlerFoundException('handle by handler', '', null)
    }

    @GetMapping('/trouble/4')
    ResponseEntity test4() {
        throw new ErrMsgException('empty stack')
    }

    @GetMapping("/json/entity")
    ResponseEntity<TestEntity> entity() {
        responseOfGet(new TestEntity('E1', 'E2'))
    }

    class TestEntity {
        TestEntity(String notIgnored, String ignored) {
            this.notIgnored = notIgnored
            this.ignored = ignored
        }
        String notIgnored
        @JsonIgnore
        String ignored
    }

    @GetMapping("/datatrunk")
    ResponseEntity<DataTrunk<String>> datatrunk() {
        responseOfGet(['1', '2', '3'], 10)
    }

}
