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
                [userId: '110000', userName: 'R', type: 'XD06A', age: '290125', regn: 'YEM', chgrea: '2203', traf: '1207', dept: '5104'],
                [userId: '110106', userName: '04', type: 'XG03A', age: '290125', regn: 'TUV', chgrea: '80', traf: '2100', dept: '5014'],
                [userId: '130000', userName: '71', type: 'XG03A', age: '290117', regn: 'CHE', chgrea: '6310', traf: '4300', dept: '3202'],
                [admdvs: 'admdvs', indu: 'indu', pham: 'pham', dosform: 'dosform', regn: 'regn', chgrea: 'chgrea', traf: 'traf', dept: 'dept']
        ]]
    }

    @GetMapping('/user/queryUserListByPage')
    Map test() {
        queryUserByPageQuery(null)
    }

}
