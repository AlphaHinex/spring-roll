package io.github.springroll.export.excel

import com.alibaba.excel.EasyExcel
import io.github.springroll.utils.JsonUtil
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

@RestController
class DebugController {

    @GetMapping
    void hello(HttpServletResponse response) {
        String exportFileName = URLEncoder.encode('hello world', 'utf-8');
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + exportFileName + ".xlsx");

        EasyExcel.write(response.getOutputStream()).head([['hello']]).sheet().doWrite([['world']]);
    }

    @PostMapping
    void post(HttpServletResponse response) {
        hello(response)
    }

    @PostMapping("/user/queryUserListByPage")
    Map queryUserByPageQuery(@RequestBody Map pageQuery) {
        println JsonUtil.toJsonIgnoreException(pageQuery)
        [rows: [
                [userName: 'Jordan', age: '23'],
                [userName: 'Kobe', age: '8']
        ]]
    }

}
