package com.proper.checkstyle;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;

@Api(tags = "/controller")
@Controller
public class NotPassController {

    @GetMapping
    public String errGet() {
        return "";
    }

    @PutMapping
    public String errPut() {
        return "";
    }

    @PostMapping
    public String errPost() {
        return "";
    }

    @DeleteMapping
    public String errDel() {
        return "";
    }

    @RequestMapping
    public String errReq() {
        return "";
    }

    private void priMethod1() {
        return;
    }

    private void priMethod2() {
        return;
    }

}
