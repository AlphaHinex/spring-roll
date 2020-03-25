package io.github.springroll.export.excel

import com.alibaba.excel.EasyExcel
import io.github.springroll.utils.JsonUtil
import org.apache.commons.lang3.RandomUtils
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
        def admdvs = ['110000', '110106', '130000']
        def indu = ['R', '04', '71']
        def pham = ['XD06A', 'XG03A', 'XG03A']
        def dosform = ['290125', '290125', '290117']
        def regn = ['YEM', 'TUV', 'CHE']
        def chgrea = ['2203', '80', '6310']
        def traf = ['1207', '2100', '4300']
        def dept = ['5104', '5014', '3202']

        def rows = []
        1000000.times {
            rows << [
                    userId: admdvs.get(RandomUtils.nextInt(0, 3)),
                    userName: indu.get(RandomUtils.nextInt(0, 3)),
                    type: pham.get(RandomUtils.nextInt(0, 3)),
                    age: dosform.get(RandomUtils.nextInt(0, 3)),
                    col01: admdvs.get(RandomUtils.nextInt(0, 3)),
                    col02: indu.get(RandomUtils.nextInt(0, 3)),
                    col03: pham.get(RandomUtils.nextInt(0, 3)),
                    col04: dosform.get(RandomUtils.nextInt(0, 3)),
                    col05: admdvs.get(RandomUtils.nextInt(0, 3)),
                    col06: indu.get(RandomUtils.nextInt(0, 3)),
                    col07: pham.get(RandomUtils.nextInt(0, 3)),
                    col08: dosform.get(RandomUtils.nextInt(0, 3)),
                    col09: admdvs.get(RandomUtils.nextInt(0, 3)),
                    col10: indu.get(RandomUtils.nextInt(0, 3)),
            ]
        }
        [rows: rows]
    }

    @GetMapping('/user/queryUserListByPage')
    Map test() {
        queryUserByPageQuery(null)
    }

}
