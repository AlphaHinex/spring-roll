package io.github.springroll.utils.http.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping('/test/proxy')
class ReverseProxyTestController {

    @PostMapping('/echo')
    String echo(HttpServletRequest request, HttpServletResponse response) {
        request.getHeaderNames().each {
            response.addHeader(it, request.getHeader(it))
        }

        return new String(request.getInputStream().bytes)
    }

    @RequestMapping(method = RequestMethod.OPTIONS, path = '/options')
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void options() { }

    @PutMapping('/echo')
    String hasBody(@RequestParam(value = "s2ibinary", required = false) MultipartFile file) {
        file.getSize() > 0 ? "TRUE" : "FALSE"
    }

}
